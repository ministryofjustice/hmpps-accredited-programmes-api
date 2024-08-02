package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param description
 * @param referralCategoryCode
 */
data class ReferralStatusReason(

  @Schema(example = "DUPLICATE", required = true, description = "")
  @get:JsonProperty("code", required = true) val code: kotlin.String,

  @Schema(example = "Duplicate referral", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: kotlin.String,

  @Schema(example = "ADMIN", required = true, description = "")
  @get:JsonProperty("referralCategoryCode", required = true) val referralCategoryCode: kotlin.String,
)
