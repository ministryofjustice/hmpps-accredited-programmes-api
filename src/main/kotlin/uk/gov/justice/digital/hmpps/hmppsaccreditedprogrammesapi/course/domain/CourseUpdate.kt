package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

data class CourseUpdate(
  val name: String,
  val description: String,
  val identifier: String,
  val audience: String,
  val alternateName: String? = null,
  val referable: Boolean = true,
)