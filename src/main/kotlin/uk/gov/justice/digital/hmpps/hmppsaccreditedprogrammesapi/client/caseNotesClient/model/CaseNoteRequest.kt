package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model

data class CaseNoteRequest(
  val type: String,
  val subType: String,
  val occurrenceDateTime: String,
  val authorName: String,
  val text: String,
)
