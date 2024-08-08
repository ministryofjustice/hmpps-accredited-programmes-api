package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param id
 * @param name
 * @param colour
 */
data class Audience(

  @Schema(example = "e4d1a44a-9c3b-4a7c-b79c-4d8a76488eb2", description = "")
  @get:JsonProperty("id") val id: UUID? = null,

  @Schema(example = "Sexual offence", description = "")
  @get:JsonProperty("name") val name: String? = null,

  @Schema(example = "orange", description = "")
  @get:JsonProperty("colour") val colour: String? = null,
)
