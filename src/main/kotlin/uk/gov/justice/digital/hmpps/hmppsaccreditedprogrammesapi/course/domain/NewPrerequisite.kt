package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

data class NewPrerequisite(
  val name: String,
  val description: String? = null,
  val identifier: String,
)
