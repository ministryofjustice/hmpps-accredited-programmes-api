package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param status
 * @param category
 * @param reason
 * @param notes
 * @param ptUser is the user a pt user
 */
data class ReferralStatusUpdate(

  @Schema(example = "ON_HOLD_REFERRAL_SUBMITTED", required = true, description = "")
  @get:JsonProperty("status", required = true) val status: String,

  @Schema(example = "W_ADMIN", description = "")
  @get:JsonProperty("category") val category: String? = null,

  @Schema(example = "Duplicate referral", description = "")
  @get:JsonProperty("reason") val reason: String? = null,

  @Schema(example = "E2E test put on hold reason", description = "")
  @get:JsonProperty("notes") val notes: String? = null,

  @Schema(example = "false", description = "is the user a pt user")
  @get:JsonProperty("ptUser") val ptUser: Boolean? = false,
)
