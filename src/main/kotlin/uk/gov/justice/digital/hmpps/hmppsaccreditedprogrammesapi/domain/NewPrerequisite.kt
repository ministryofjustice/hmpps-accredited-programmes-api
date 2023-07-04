package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class NewPrerequisite(
  val name: String,
  val course: String,
  val description: String? = null,
  val comments: String? = null,
)
