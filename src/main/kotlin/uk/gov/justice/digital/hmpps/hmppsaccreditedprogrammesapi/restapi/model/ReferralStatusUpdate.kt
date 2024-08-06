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

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("status", required = true) val status: kotlin.String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("category") val category: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("reason") val reason: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("notes") val notes: kotlin.String? = null,

  @Schema(example = "null", description = "is the user a pt user")
  @get:JsonProperty("ptUser") val ptUser: kotlin.Boolean? = false,
)
