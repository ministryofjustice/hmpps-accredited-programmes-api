package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

data class ReferralUpdate(
  val additionalInformation: String?,
  val oasysConfirmed: Boolean,
  val hasReviewedProgrammeHistory: Boolean,
  val hasReviewedAdditionalInformation: Boolean? = null,
  val referrerOverrideReason: String? = null,
  val recommendedPathway: String? = null,
  val requestedPathway: String? = null,
  val hasLdc: Boolean? = null,
  val hasLdcBeenOverriddenByProgrammeTeam: Boolean? = null,
)
