package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param description
 * @param referralStatusCode
 */
data class ReferralStatusCategory(

  @Schema(example = "ADMIN", required = true, description = "")
  @get:JsonProperty("code", required = true) val code: kotlin.String,

  @Schema(example = "Administrative error", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: kotlin.String,

  @Schema(example = "WITHDRAWN", required = true, description = "")
  @get:JsonProperty("referralStatusCode", required = true) val referralStatusCode: kotlin.String,
)
