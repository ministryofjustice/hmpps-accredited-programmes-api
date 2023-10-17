package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "offering")
class OfferingEntity(
  @Id
  @GeneratedValue
  @Column(name = "offering_id")
  val id: UUID? = null,

  val organisationId: String,
  var contactEmail: String,
  var secondaryContactEmail: String? = null,
  var withdrawn: Boolean = false,
) {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id")
  lateinit var course: CourseEntity

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is OfferingEntity) return false
    return organisationId == other.organisationId
  }

  override fun hashCode(): Int = organisationId.hashCode()
}
