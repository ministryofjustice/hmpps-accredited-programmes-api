package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import java.util.UUID
import kotlin.time.Duration

class Offering(
  val id: UUID = UUID.randomUUID(),
  val organisationId: String,
  val duration: Duration,
  val groupSize: Int,
  val contactEmail: String,
  val course: CourseEntity,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Offering) return false
    return this.id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
