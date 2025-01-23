package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param oasysConfirmed
 * @param hasReviewedProgrammeHistory
 * @param additionalInformation
 * @param overrideReason
 */
data class ReferralUpdate(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("oasysConfirmed", required = true) val oasysConfirmed: Boolean = false,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hasReviewedProgrammeHistory", required = true) val hasReviewedProgrammeHistory: Boolean = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("additionalInformation") val additionalInformation: String? = null,

  @Schema(example = "The reason for going with the recommended course is...", description = "Reason for overriding the recommended course")
  @get:JsonProperty("overrideReason") val overrideReason: String? = null,
)
