package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CaseNoteCreatedResponse(
  val caseNoteId: String,
)
