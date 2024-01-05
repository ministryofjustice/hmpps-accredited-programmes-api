package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.CacheKeySet
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

@Component
class WebClientCache(
  private val objectMapper: ObjectMapper,
  private val redisTemplate: RedisTemplate<String, String>,
  @Value("\${preemptive-cache-key-prefix}") private val preemptiveCacheKeyPrefix: String,
) {
  fun <ResponseType : Any> tryGetCachedValue(
    typeReference: TypeReference<ResponseType>,
    requestBuilder: BaseHMPPSClient.HMPPSRequestConfiguration,
    cacheConfig: PreemptiveCacheConfig,
    attempt: AtomicInteger,
  ): ClientResult<ResponseType>? {
    val cacheKeySet = getCacheKeySet(requestBuilder, cacheConfig)

    val cacheEntry = getCacheEntryMetadataIfExists(cacheKeySet.metadataKey)

    attempt.set(cacheEntry?.attempt?.plus(1) ?: 1)

    if (cacheEntry != null && cacheEntry.refreshableAfter.isAfter(Instant.now())) {
      return resultFromCacheMetadata(cacheEntry, cacheKeySet, typeReference)
    }

    return null
  }

  private fun getCacheKeySet(
    requestBuilder: BaseHMPPSClient.HMPPSRequestConfiguration,
    cacheConfig: PreemptiveCacheConfig,
  ): CacheKeySet {
    val key = requestBuilder.preemptiveCacheKey ?: throw RuntimeException("Must provide a preemptiveCacheKey")
    return CacheKeySet(preemptiveCacheKeyPrefix, cacheConfig.cacheName, key)
  }

  private fun getCacheEntryMetadataIfExists(metaDataKey: String): PreemptiveCacheMetadata? {
    val stringValue = redisTemplate.boundValueOps(metaDataKey).get()
      ?: return null

    return objectMapper.readValue<PreemptiveCacheMetadata>(
      stringValue,
    )
  }

  private fun getCacheEntryBody(dataKey: String): String? {
    return redisTemplate.boundValueOps(dataKey).get()
  }

  private fun <ResponseType> resultFromCacheMetadata(
    cacheEntry: PreemptiveCacheMetadata,
    cacheKeySet: CacheKeySet,
    typeReference: TypeReference<ResponseType>,
  ): ClientResult<ResponseType> {
    val cachedBody = if (cacheEntry.hasResponseBody) {
      getCacheEntryBody(cacheKeySet.dataKey) ?: return ClientResult.Failure.CachedValueUnavailable(
        cacheKey = cacheKeySet.dataKey,
      )
    } else {
      null
    }

    if (cacheEntry.httpStatus.is2xxSuccessful) {
      return ClientResult.Success(
        status = cacheEntry.httpStatus,
        body = objectMapper.readValue(cachedBody!!, typeReference),
      )
    }

    return ClientResult.Failure.StatusCode(
      status = cacheEntry.httpStatus,
      body = cachedBody,
      method = cacheEntry.method!!,
      path = cacheEntry.path!!,
    )
  }

  data class PreemptiveCacheConfig(
    val cacheName: String,
    val successSoftTtlSeconds: Int,
    val failureSoftTtlBackoffSeconds: List<Int>,
    val hardTtlSeconds: Int,
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class PreemptiveCacheMetadata(
    val httpStatus: HttpStatus,
    val refreshableAfter: Instant,
    val method: HttpMethod?,
    val path: String?,
    val hasResponseBody: Boolean,
    val attempt: Int?,
  )
}
