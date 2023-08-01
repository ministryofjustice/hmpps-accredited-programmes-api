package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class PrerequisiteUpdate(
  val name: String,
  val description: String? = null,
  val identifier: String,
)
