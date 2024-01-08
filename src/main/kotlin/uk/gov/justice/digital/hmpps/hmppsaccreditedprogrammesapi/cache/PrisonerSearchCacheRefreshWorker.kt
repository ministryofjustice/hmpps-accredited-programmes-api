package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache

import redis.lock.redlock.RedLock
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository

class PrisonerSearchCacheRefreshWorker(
  private val referralRepository: ReferralRepository,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val loggingEnabled: Boolean,
  private val delayMs: Long,
  redLock: RedLock,
  lockDurationMs: Int,
) : CacheRefreshWorker(redLock, "prisonerDetails", lockDurationMs) {
  override fun work(checkShouldStop: () -> Boolean) {
    val distinctPrisonerNumbers =
      referralRepository.getDistinctPrisonNumbers()

    if (loggingEnabled) { log.info("Got $distinctPrisonerNumbers to refresh for prisoner Details from referral summary") }

    val prisoners = prisonerSearchApiService.getPrisoners(distinctPrisonerNumbers)

    log.info("Cached ${prisoners.size} prisoners")
    interruptableSleep(delayMs)
  }
}
