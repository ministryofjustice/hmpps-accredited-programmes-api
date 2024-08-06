package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param type
 * @param code
 * @param description
 * @param earliestReleaseDate
 * @param date
 * @param order
 */
data class KeyDate(

  @Schema(example = "earliestReleaseDate", required = true, description = "")
  @get:JsonProperty("type", required = true) val type: kotlin.String,

  @Schema(example = "ERD", required = true, description = "")
  @get:JsonProperty("code", required = true) val code: kotlin.String,

  @Schema(example = "Earliest Release Date", description = "")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("earliestReleaseDate") val earliestReleaseDate: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("date") val date: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("order") val order: kotlin.Int? = null,
)
