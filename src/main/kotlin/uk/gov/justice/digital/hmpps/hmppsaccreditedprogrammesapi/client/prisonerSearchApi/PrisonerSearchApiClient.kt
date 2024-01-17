package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache.WebClientCache
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerNumbers
import java.time.Duration

@Component
class PrisonerSearchApiClient(
  @Qualifier("prisonerSearchApiWebClient")
  webClient: WebClient,
  webClientCache: WebClientCache,
) : BaseHMPPSClient(webClient, jacksonObjectMapper(), webClientCache) {

  private val prisonerSearchApiCacheConfig = WebClientCache.PreemptiveCacheConfig(
    cacheName = "prisonerDetails",
    successSoftTtlSeconds = Duration.ofHours(6).toSeconds().toInt(),
    failureSoftTtlBackoffSeconds =
    listOf(
      30,
      Duration.ofMinutes(5).toSeconds().toInt(),
      Duration.ofMinutes(10).toSeconds().toInt(),
      Duration.ofMinutes(30).toSeconds().toInt(),
    ),
    hardTtlSeconds = Duration.ofHours(12).toSeconds().toInt(),
  )

  fun getPrisonersByPrisonNumbers(prisonNumbers: List<String>) = postRequest<List<Prisoner>> {
    path = "/prisoner-search/prisoner-numbers"
    body = PrisonerNumbers(prisonNumbers)
  }

  fun getPrisonerDetailsCacheEntryStatus(crn: String) = checkPreemptiveCacheStatus(prisonerSearchApiCacheConfig, crn)

  fun getPrisonersByPrisonNumbersWait(crn: String)  = postRequest<List<Prisoner>> {
    preemptiveCacheConfig = prisonerSearchApiCacheConfig
    preemptiveCacheKey = crn
    preemptiveCacheTimeoutMs = 0
  }

  fun getPrisonersByPrisonNumbers(key: String, prisonNumbers: List<String>) = postRequest<List<Prisoner>> {
    path = "/prisoner-search/prisoner-numbers"
    isPreemptiveCall = true
    preemptiveCacheConfig = prisonerSearchApiCacheConfig
    preemptiveCacheKey = key
    body = PrisonerNumbers(prisonNumbers)
  }

}
