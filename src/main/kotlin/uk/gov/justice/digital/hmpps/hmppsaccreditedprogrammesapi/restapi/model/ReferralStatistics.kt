package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository.ReferralStatisticsProjection

data class ReferralStatistics(
  val submittedReferralCount: Long,
  val draftReferralCount: Long,
  val averageDuration: String,
) {
  companion object {
    fun from(referralStatistics: ReferralStatisticsProjection): ReferralStatistics = ReferralStatistics(
      submittedReferralCount = referralStatistics.getSubmittedReferrals().toLong(),
      draftReferralCount = referralStatistics.getDraftReferrals().toLong(),
      averageDuration = referralStatistics.getAverageDuration(),
    )
  }
}
