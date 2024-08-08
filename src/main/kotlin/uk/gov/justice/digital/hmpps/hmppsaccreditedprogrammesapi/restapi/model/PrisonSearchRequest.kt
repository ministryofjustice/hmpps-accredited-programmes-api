package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prisonIds List of Prisons
 */
data class PrisonSearchRequest(

  @Schema(example = "[\"MDI\"]", required = true, description = "List of Prisons")
  @get:JsonProperty("prisonIds", required = true) val prisonIds: List<String>,
)
