package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class PeopleSearchRequest(

  @Schema(
    example = "A1234AA",
    description = "Prisoner identifier, the NOMIS id",
  )
  val prisonerIdentifier: String? = null,

  @Schema(
    example = "[\"MDI\"]",
    description = "List of Prison Ids (can include OUT and TRN) to restrict the search by. Unrestricted if not supplied or null",
  )
  val prisonIds: List<String>? = null,

  @Schema(
    example = "John",
    description = "First name of the prisoner",
  )
  val firstName: String? = null,
  @Schema(
    example = "Doe",
    description = "Last name of the prisoner",
  )
  val lastName: String? = null,
)
