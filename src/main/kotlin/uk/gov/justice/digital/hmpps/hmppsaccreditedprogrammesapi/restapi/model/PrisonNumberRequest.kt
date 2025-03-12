package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class PrisonNumberRequest(
  @Schema(example = "null", description = "List of prison numbers for which comparison needs to be checked")
  @get:JsonProperty("prisonNumbers")
  val prisonNumbers: List<String>,
)
