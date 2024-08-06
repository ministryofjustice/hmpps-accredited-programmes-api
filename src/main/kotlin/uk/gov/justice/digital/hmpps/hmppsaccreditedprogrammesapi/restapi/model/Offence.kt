package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offence Description of the offence along with the code.
 * @param category Legislation.
 * @param offenceDate Offence start date.
 */
data class Offence(

  @Schema(example = "null", description = "Description of the offence along with the code.")
  @get:JsonProperty("offence") val offence: kotlin.String? = null,

  @Schema(example = "null", description = "Legislation.")
  @get:JsonProperty("category") val category: kotlin.String? = null,

  @Schema(example = "null", description = "Offence start date.")
  @get:JsonProperty("offenceDate") val offenceDate: java.time.LocalDate? = null,
)
