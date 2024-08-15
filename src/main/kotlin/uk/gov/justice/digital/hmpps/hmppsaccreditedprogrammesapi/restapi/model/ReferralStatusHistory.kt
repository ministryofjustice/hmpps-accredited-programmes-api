package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

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
  @get:JsonProperty("id") val id: UUID? = null,

  @Schema(example = "null", description = "The unique id (UUID) of the referral.")
  @get:JsonProperty("referralId") val referralId: UUID? = null,

  @Schema(example = "null", description = "The status of the referral.")
  @get:JsonProperty("status") val status: String? = null,

  @Schema(example = "null", description = "The status description.")
  @get:JsonProperty("statusDescription") val statusDescription: String? = null,

  @Schema(example = "null", description = "The colour to display status description.")
  @get:JsonProperty("statusColour") val statusColour: String? = null,

  @Schema(example = "null", description = "The previous status of the referral.")
  @get:JsonProperty("previousStatus") val previousStatus: String? = null,

  @Schema(example = "null", description = "The previous status description.")
  @get:JsonProperty("previousStatusDescription") val previousStatusDescription: String? = null,

  @Schema(example = "null", description = "The previous colour to display status description.")
  @get:JsonProperty("previousStatusColour") val previousStatusColour: String? = null,

  @Schema(example = "null", description = "The notes associated with the status change.")
  @get:JsonProperty("notes") val notes: String? = null,

  @Schema(example = "null", description = "Date referral was changed to this status.")
  @get:JsonProperty("statusStartDate") val statusStartDate: java.time.Instant? = null,

  @Schema(example = "null", description = "Username of the person who changed to this status")
  @get:JsonProperty("username") val username: String? = null,

  @Schema(example = "null", description = "The description of the category - if appropriate.")
  @get:JsonProperty("categoryDescription") val categoryDescription: String? = null,

  @Schema(example = "null", description = "The description of the reason - if appropriate.")
  @get:JsonProperty("reasonDescription") val reasonDescription: String? = null,
)
