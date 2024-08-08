package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prisonerIdentifier Prisoner identifier for this case we only accept prison number
 * @param prisonIds List of Prison Ids (can include OUT and TRN) to restrict the search by. Unrestricted if not supplied or null
 */
data class PeopleSearchRequest(

  @Schema(example = "A1234AA", required = true, description = "Prisoner identifier for this case we only accept prison number")
  @get:JsonProperty("prisonerIdentifier", required = true) val prisonerIdentifier: String,

  @Schema(example = "[\"MDI\"]", description = "List of Prison Ids (can include OUT and TRN) to restrict the search by. Unrestricted if not supplied or null")
  @get:JsonProperty("prisonIds") val prisonIds: List<String>? = null,
)
