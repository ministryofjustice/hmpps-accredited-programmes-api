package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

data class PrerequisiteUpdate(
  val name: String,
  val description: String? = null,
  val identifier: String,
)
