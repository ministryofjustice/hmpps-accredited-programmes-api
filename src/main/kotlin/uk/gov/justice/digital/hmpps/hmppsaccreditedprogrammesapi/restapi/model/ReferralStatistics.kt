package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository.ReferralStatisticsProjection
import java.math.BigInteger

data class ReferralStatistics(
  val submittedReferralCount: BigInteger,
  val draftReferralCount: BigInteger,
  val averageDuration: String,
) {
  companion object {
    fun from(referralStatistics: ReferralStatisticsProjection): ReferralStatistics = ReferralStatistics(
      submittedReferralCount = referralStatistics.getSubmittedReferrals(),
      draftReferralCount = referralStatistics.getDraftReferrals(),
      averageDuration = referralStatistics.getAverageDuration(),
    )
  }
}
