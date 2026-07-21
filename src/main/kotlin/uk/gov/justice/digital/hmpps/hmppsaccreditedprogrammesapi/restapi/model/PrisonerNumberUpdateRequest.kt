package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

data class PrisonerNumberUpdateRequest(
  val currentPrisonerNumber: String,
  val newPrisonerNumber: String,
)
