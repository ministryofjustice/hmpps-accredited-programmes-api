package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param description
 */
data class PrisonType(

  @Schema(example = "YOI", required = true, description = "")
  @get:JsonProperty("code", required = true) val code: kotlin.String,

  @Schema(example = "His Majesty’s Youth Offender Institution", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: kotlin.String,
)
