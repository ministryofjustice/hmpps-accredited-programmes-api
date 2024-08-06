package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param id The id (UUID) of the status history record.
 * @param referralId The unique id (UUID) of the referral.
 * @param status The status of the referral.
 * @param statusDescription The status description.
 * @param statusColour The colour to display status description.
 * @param previousStatus The previous status of the referral.
 * @param previousStatusDescription The previous status description.
 * @param previousStatusColour The previous colour to display status description.
 * @param notes The notes associated with the status change.
 * @param statusStartDate Date referral was changed to this status.
 * @param username Username of the person who changed to this status
 */
data class ReferralStatusHistory(

  @Schema(example = "null", description = "The id (UUID) of the status history record.")
  @get:JsonProperty("id") val id: java.util.UUID? = null,

  @Schema(example = "null", description = "The unique id (UUID) of the referral.")
  @get:JsonProperty("referralId") val referralId: java.util.UUID? = null,

  @Schema(example = "null", description = "The status of the referral.")
  @get:JsonProperty("status") val status: kotlin.String? = null,

  @Schema(example = "null", description = "The status description.")
  @get:JsonProperty("statusDescription") val statusDescription: kotlin.String? = null,

  @Schema(example = "null", description = "The colour to display status description.")
  @get:JsonProperty("statusColour") val statusColour: kotlin.String? = null,

  @Schema(example = "null", description = "The previous status of the referral.")
  @get:JsonProperty("previousStatus") val previousStatus: kotlin.String? = null,

  @Schema(example = "null", description = "The previous status description.")
  @get:JsonProperty("previousStatusDescription") val previousStatusDescription: kotlin.String? = null,

  @Schema(example = "null", description = "The previous colour to display status description.")
  @get:JsonProperty("previousStatusColour") val previousStatusColour: kotlin.String? = null,

  @Schema(example = "null", description = "The notes associated with the status change.")
  @get:JsonProperty("notes") val notes: kotlin.String? = null,

  @Schema(example = "null", description = "Date referral was changed to this status.")
  @get:JsonProperty("statusStartDate") val statusStartDate: java.time.Instant? = null,

  @Schema(example = "null", description = "Username of the person who changed to this status")
  @get:JsonProperty("username") val username: kotlin.String? = null,
)
