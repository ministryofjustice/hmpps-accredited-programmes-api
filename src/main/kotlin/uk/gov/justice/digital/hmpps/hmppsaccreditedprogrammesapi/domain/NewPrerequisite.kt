package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class NewPrerequisite(
  val name: String,
  val description: String? = null,
  val identifier: String,
)
