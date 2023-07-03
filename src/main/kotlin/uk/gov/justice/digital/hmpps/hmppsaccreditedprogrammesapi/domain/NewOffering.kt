package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class NewOffering(
  val prisonId: String,
  val identifier: String,
  val contactEmail: String? = null,
)
