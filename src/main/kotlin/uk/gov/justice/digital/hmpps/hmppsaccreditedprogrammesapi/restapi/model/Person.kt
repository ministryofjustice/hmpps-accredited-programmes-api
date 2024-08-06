package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prisonNumber The prison number of the person who is being referred.
 */
data class Person(

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: kotlin.String,
)
