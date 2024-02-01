package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

data class CourseUpdate(
  val name: String,
  val description: String,
  val identifier: String,
  val audience: String,
  val alternateName: String? = null,
)
