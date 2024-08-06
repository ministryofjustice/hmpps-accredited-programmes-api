package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offeringId The id (UUID) of an active offering
 * @param prisonNumber The prison number of the person who is being referred.
 * @param oasysConfirmed
 * @param hasReviewedProgrammeHistory
 * @param id The unique id (UUID) of this referral.
 * @param status The status code of the referral.
 * @param referrerUsername
 * @param additionalInformation
 * @param closed Is the status of the referral a closed one.
 * @param statusDescription The status description.
 * @param statusColour The colour to display status description.
 * @param submittedOn
 */
data class Referral(

  @Schema(example = "null", required = true, description = "The id (UUID) of an active offering")
  @get:JsonProperty("offeringId", required = true) val offeringId: java.util.UUID,

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: kotlin.String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("oasysConfirmed", required = true) val oasysConfirmed: kotlin.Boolean = false,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hasReviewedProgrammeHistory", required = true) val hasReviewedProgrammeHistory: kotlin.Boolean = false,

  @Schema(example = "null", required = true, description = "The unique id (UUID) of this referral.")
  @get:JsonProperty("id", required = true) val id: java.util.UUID,

  @Schema(example = "null", required = true, description = "The status code of the referral.")
  @get:JsonProperty("status", required = true) val status: kotlin.String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("referrerUsername", required = true) val referrerUsername: kotlin.String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("additionalInformation") val additionalInformation: kotlin.String? = null,

  @Schema(example = "null", description = "Is the status of the referral a closed one.")
  @get:JsonProperty("closed") val closed: kotlin.Boolean? = null,

  @Schema(example = "null", description = "The status description.")
  @get:JsonProperty("statusDescription") val statusDescription: kotlin.String? = null,

  @Schema(example = "null", description = "The colour to display status description.")
  @get:JsonProperty("statusColour") val statusColour: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("submittedOn") val submittedOn: kotlin.String? = null,
)
