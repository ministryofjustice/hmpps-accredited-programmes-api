package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param oasysConfirmed
 * @param hasReviewedProgrammeHistory
 * @param additionalInformation
 * @param referrerOverrideReason
 */
data class ReferralUpdate(

  @Schema(example = "true", nullable = false, description = "A human user has confirmed that the OASys information is correct")
  @get:JsonProperty("oasysConfirmed", required = true) val oasysConfirmed: Boolean = false,

  @Schema(example = "true", nullable = false, description = "A human user has reviewed the Programme History for a Referral")
  @get:JsonProperty("hasReviewedProgrammeHistory", required = true) val hasReviewedProgrammeHistory: Boolean = false,

  @Schema(example = "null", description = "Any possibly useful information, from a referrer about the Referral", nullable = true)
  @get:JsonProperty("additionalInformation") val additionalInformation: String? = null,

  @Schema(example = "The reason for NOT going with the recommended course is...", description = "Reason for overriding the recommended course", nullable = true)
  @get:JsonProperty("referrerOverrideReason") val referrerOverrideReason: String? = null,

  @Schema(example = "true", description = "Flag to indicate if the person has a Learning Difficulty Challenges", nullable = true)
  @get:JsonProperty("hasLdc") val hasLdc: Boolean? = null,

  @Schema(example = "true", nullable = true, description = "Flag to indicate if the ldc field was overriden by the programme team")
  @get:JsonProperty("hasLdcBeenOverriddenByProgrammeTeam") val hasLdcBeenOverriddenByProgrammeTeam: Boolean? = null,

  @Schema(example = "true", description = "Flag to indicate if the user has reviewed the additional information", nullable = true)
  @get:JsonProperty("hasReviewedAdditionalInformation") val hasReviewedAdditionalInformation: Boolean? = null,
)
