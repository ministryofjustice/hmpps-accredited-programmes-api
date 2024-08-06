package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param oasysConfirmed
 * @param hasReviewedProgrammeHistory
 * @param additionalInformation
 */
data class ReferralUpdate(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("oasysConfirmed", required = true) val oasysConfirmed: kotlin.Boolean = false,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hasReviewedProgrammeHistory", required = true) val hasReviewedProgrammeHistory: kotlin.Boolean = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("additionalInformation") val additionalInformation: kotlin.String? = null,
)
