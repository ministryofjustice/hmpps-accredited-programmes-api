package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param name
 */
data class PrisonOperator(

  @Schema(example = "PSP, G4S", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: kotlin.String,
)
