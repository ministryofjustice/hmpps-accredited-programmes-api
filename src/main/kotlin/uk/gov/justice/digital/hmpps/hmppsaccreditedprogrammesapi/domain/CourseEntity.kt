package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import java.util.UUID

class CourseEntity(
  val id: UUID = UUID.randomUUID(),
  val name: String,
  val type: String,
  val description: String? = null,
  val prerequisites: List<Prerequisite>,
  val audience: List<Audience>,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CourseEntity) return false
    return this.id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
