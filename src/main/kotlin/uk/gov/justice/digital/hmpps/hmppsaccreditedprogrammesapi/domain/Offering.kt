package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Offering(
  @Id
  @GeneratedValue
  @Column(name = "offering_id")
  var id: UUID? = null,

  var organisationId: String,

  var contactEmail: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  var course: CourseEntity,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Offering) return false
    return id == other.id
  }

  override fun hashCode(): Int = 1756406093
}
