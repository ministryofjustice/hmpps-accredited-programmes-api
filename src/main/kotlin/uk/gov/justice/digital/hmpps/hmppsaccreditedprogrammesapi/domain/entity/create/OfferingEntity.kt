package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "offering")
class OfferingEntity(
  @Id
  @Column(name = "offering_id")
  var id: UUID? = null,

  var organisationId: String,
  var contactEmail: String,
  var secondaryContactEmail: String? = null,
  var withdrawn: Boolean = false,
  var referable: Boolean = true,

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "course_id")
  var course: CourseEntity,
) {
  @PrePersist
  fun generateId() {
    if (id == null) {
      id = UUID.randomUUID()
    }
  }
}
