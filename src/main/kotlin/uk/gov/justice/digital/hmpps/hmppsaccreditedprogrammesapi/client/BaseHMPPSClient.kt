package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ConnectTimeoutException
import io.netty.handler.timeout.ReadTimeoutException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.PrematureCloseException
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import java.io.IOException
import java.time.Duration

abstract class BaseHMPPSClient(
  private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  // Bounded retry-with-backoff for transient, pre-response connection drops (e.g. ARNS
  // "Connection prematurely closed BEFORE response", connection resets). Only safe (GET) requests
  // are retried. We deliberately do NOT retry read/connect timeouts: those burn the full upstream
  // timeout, so retrying them would exceed the caller's (UI) deadline and stack with its own
  // retries, hammering a struggling upstream. 4xx/5xx are WebClientResponseException and so are
  // never retried here. `open` so tests can shorten the backoff; production clients inherit these.
  protected open val maxRetryAttempts: Long = 2
  protected open val retryBackoff: Duration = Duration.ofMillis(500)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  init {
    // Tolerate unknown fields across all HMPPS upstreams. Upstream services evolve their
    // schemas independently of us (e.g. APG-2707's ARNS "assessmentType" field), so a new
    // field must never fail deserialisation and 500 our endpoints. Applied centrally so
    // every client is resilient, not just the one that previously incurred an incident.
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  protected inline fun <reified ResponseType : Any> getRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.GET, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> postRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.POST, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> putRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.PUT, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> deleteRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.DELETE, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> patchRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> = request(HttpMethod.PATCH, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> request(
    method: HttpMethod,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String,
  ): ClientResult<ResponseType> {
    val typeReference = object : TypeReference<ResponseType>() {}

    return doRequest(typeReference, method, requestBuilderConfiguration, serviceName)
  }

  fun <ResponseType : Any> doRequest(
    typeReference: TypeReference<ResponseType>,
    method: HttpMethod,
    requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String,
  ): ClientResult<ResponseType> {
    val requestBuilder = HMPPSRequestConfiguration()
    requestBuilderConfiguration(requestBuilder)

    try {
      val request = webClient.method(method)
        .uri(requestBuilder.path ?: "")
        .headers { it.addAll(requestBuilder.headers) }

      if (requestBuilder.body != null) {
        request.bodyValue(requestBuilder.body!!)
      }

      val result = request.retrieve()
        .toEntity(String::class.java)
        .retryWhen(
          Retry.backoff(maxRetryAttempts, retryBackoff)
            .jitter(0.5)
            .filter { method == HttpMethod.GET && isTransientConnectionDrop(it) }
            .onRetryExhaustedThrow { _, retrySignal -> retrySignal.failure() },
        )
        .block()!!

      objectMapper.apply { registerModule((JavaTimeModule())) }
      val deserialized = objectMapper.readValue(result.body, typeReference)

      return ClientResult.Success(result.statusCode, deserialized)
    } catch (exception: WebClientResponseException) {
      if (exception.statusCode.is5xxServerError) {
        log.error("Request to $serviceName failed with status code ${exception.statusCode.value()} reason ${exception.message}.")
        throw ServiceUnavailableException("$serviceName is temporarily unavailable. Please try again later.", exception)
      } else if (!exception.statusCode.is2xxSuccessful) {
        return ClientResult.Failure.StatusCode(
          method,
          requestBuilder.path ?: "",
          exception.statusCode,
          exception.responseBodyAsString,
        )
      } else {
        log.error("Request to $serviceName failed with status code ${exception.statusCode.value()} reason ${exception.message}.")
        throw exception
      }
    } catch (exception: Exception) {
      return ClientResult.Failure.Other(method, requestBuilder.path ?: "", exception, serviceName)
    }
  }

  private fun isTransientConnectionDrop(throwable: Throwable): Boolean {
    if (throwable !is WebClientRequestException) return false
    val cause = throwable.cause
    // Read/connect timeouts consume the full upstream timeout, so retrying them would blow the
    // caller's deadline and compound with its own retries. Only retry fast connection drops.
    if (cause is ReadTimeoutException || cause is ConnectTimeoutException) return false
    return cause is PrematureCloseException || cause is IOException
  }

  class HMPPSRequestConfiguration {
    internal var path: String? = null
    internal var body: Any? = null
    internal var headers = HttpHeaders()
    fun withHeader(key: String, value: String) = headers.add(key, value)
  }
}

sealed interface ClientResult<ResponseType> {
  class Success<ResponseType>(val status: HttpStatusCode, val body: ResponseType) : ClientResult<ResponseType>
  sealed interface Failure<ResponseType> : ClientResult<ResponseType> {
    fun throwException(): Nothing = throw toException()
    fun toException(): Throwable

    class StatusCode<ResponseType>(
      val method: HttpMethod,
      val path: String,
      val status: HttpStatusCode,
      val body: String?,
    ) : Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("Unable to complete $method request to $path: $status")

      inline fun <reified ResponseType> deserializeTo(): ResponseType = jacksonObjectMapper().readValue(body, ResponseType::class.java)
    }

    class Other<ResponseType>(
      val method: HttpMethod,
      val path: String,
      val exception: Exception,
      val serviceName: String,
    ) : Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("Unable to complete request. Service $serviceName for $method request to $path", exception)
    }
  }
}
