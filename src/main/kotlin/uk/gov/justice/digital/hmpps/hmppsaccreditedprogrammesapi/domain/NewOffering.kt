package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class NewOffering(
  val course: String,
  val prisonId: String,
  val organisation: String? = null,
  val contactEmail: String? = null,
)
