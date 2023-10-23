package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

data class ReferralUpdate(
  val reason: String?,
  val additionalInformation: String?,
  val oasysConfirmed: Boolean,
  val hasReviewedProgrammeHistory: Boolean,
)
