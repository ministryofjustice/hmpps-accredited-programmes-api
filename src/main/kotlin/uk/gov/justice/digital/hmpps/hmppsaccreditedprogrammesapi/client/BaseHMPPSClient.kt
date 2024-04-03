package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

abstract class BaseHMPPSClient(
  private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  protected inline fun <reified ResponseType : Any> getRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> =
    request(HttpMethod.GET, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> postRequest(
    serviceName: String,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
  ): ClientResult<ResponseType> =
    request(HttpMethod.POST, requestBuilderConfiguration, serviceName)

  protected inline fun <reified ResponseType : Any> putRequest(noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit): ClientResult<ResponseType> =
    request(HttpMethod.PUT, requestBuilderConfiguration)

  protected inline fun <reified ResponseType : Any> deleteRequest(noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit): ClientResult<ResponseType> =
    request(HttpMethod.DELETE, requestBuilderConfiguration)

  protected inline fun <reified ResponseType : Any> patchRequest(noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit): ClientResult<ResponseType> =
    request(HttpMethod.PATCH, requestBuilderConfiguration)

  protected inline fun <reified ResponseType : Any> request(
    method: HttpMethod,
    noinline requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String? = "",
  ): ClientResult<ResponseType> {
    val typeReference = object : TypeReference<ResponseType>() {}

    return doRequest(typeReference, method, requestBuilderConfiguration, serviceName)
  }

  fun <ResponseType : Any> doRequest(
    typeReference: TypeReference<ResponseType>,
    method: HttpMethod,
    requestBuilderConfiguration: HMPPSRequestConfiguration.() -> Unit,
    serviceName: String?,
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

      val result = request.retrieve().toEntity(String::class.java).block()!!

      objectMapper.apply { registerModule((JavaTimeModule())) }
      val deserialized = objectMapper.readValue(result.body, typeReference)

      return ClientResult.Success(result.statusCode, deserialized)
    } catch (exception: WebClientResponseException) {
      if (exception.statusCode.is5xxServerError) {
        throw ServiceUnavailableException("$serviceName is temporarily unavailable. Please try again later.", exception)
      } else if (!exception.statusCode.is2xxSuccessful) {
        return ClientResult.Failure.StatusCode(
          method,
          requestBuilder.path ?: "",
          exception.statusCode,
          exception.responseBodyAsString,
        )
      } else {
        throw exception
      }
    } catch (exception: Exception) {
      return ClientResult.Failure.Other(method, requestBuilder.path ?: "", exception)
    }
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

      inline fun <reified ResponseType> deserializeTo(): ResponseType =
        jacksonObjectMapper().readValue(body, ResponseType::class.java)
    }

    class CachedValueUnavailable<ResponseType>(val cacheKey: String) : Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("No Redis entry exists for $cacheKey")
    }

    class Other<ResponseType>(val method: HttpMethod, val path: String, val exception: Exception) :
      Failure<ResponseType> {
      override fun toException(): Throwable = RuntimeException("Unable to complete $method request to $path", exception)
    }
  }
}
