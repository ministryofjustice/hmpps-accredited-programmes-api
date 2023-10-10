package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.Hibernate
import java.util.UUID

@Entity
class Offering(
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
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Offering
    return organisationId == other.organisationId
  }

  override fun hashCode(): Int = organisationId.hashCode()

  public override fun toString(): String = "Offering($id, $organisationId, $contactEmail, $secondaryContactEmail, $withdrawn)"
}
