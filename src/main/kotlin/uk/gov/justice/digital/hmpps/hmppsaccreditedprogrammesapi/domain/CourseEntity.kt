package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

class CourseEntity(
  val id: java.util.UUID,
  val name: String,
  val type: String,
  val description: String? = null,
  val prerequisites: List<PrerequisiteEntity>,
)
