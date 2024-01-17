package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache

import redis.lock.redlock.RedLock
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.PreemptiveCacheEntryStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository

class PrisonerSearchCacheRefreshWorker(
  private val referralRepository: ReferralRepository,
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
  private val loggingEnabled: Boolean,
  private val delayMs: Long,
  redLock: RedLock,
  lockDurationMs: Int,
) : CacheRefreshWorker(redLock, "prisonerDetails", lockDurationMs) {
  override fun work(checkShouldStop: () -> Boolean) {
    val distinctPrisonerNumbers =
      referralRepository.getPrisonersWithOrgId()

    if (loggingEnabled) {
      log.info("Got $distinctPrisonerNumbers to refresh for prisoner Details from referral summary")
    }

    val groupBy = distinctPrisonerNumbers.groupBy { it.organisationId }

    groupBy.forEach { (key, value) ->

      val cacheEntryStatus = prisonerSearchApiClient.getPrisonerDetailsCacheEntryStatus(key)

      if (cacheEntryStatus == PreemptiveCacheEntryStatus.EXISTS) {
        log.info("Cached ${value.size} prisoners")
        return@forEach
      }

      val prisoners = when (val response = prisonerSearchApiClient.getPrisonersByPrisonNumbers(key, value.map { it.prisonNumber })) {
        is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
        is ClientResult.Failure -> AuthorisableActionResult.NotFound()
      }

       when (prisoners) {
        is AuthorisableActionResult.Unauthorised -> throw Exception("Unauthorized")
        is AuthorisableActionResult.NotFound -> throw Exception("Data not found")
        is AuthorisableActionResult.Success -> {

          val entity = prisoners.entity

          entity

        }
      }
    }


    interruptableSleep(delayMs)
  }
}