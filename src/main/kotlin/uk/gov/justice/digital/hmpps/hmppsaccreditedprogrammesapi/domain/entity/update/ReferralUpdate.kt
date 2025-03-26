package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

data class ReferralUpdate(
  val additionalInformation: String?,
  val oasysConfirmed: Boolean,
  val hasReviewedProgrammeHistory: Boolean,
  val overrideReason: String? = null,
  val hasLdc: Boolean? = null,
  val hasLdcBeenOverriddenByProgrammeTeam: Boolean? = null,
)
