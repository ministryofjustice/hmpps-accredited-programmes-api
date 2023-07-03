package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class NewCourse(
  val name: String,
  val description: String,
  val identifier: String,
  val audience: String,
  val alternateName: String? = null,
)
