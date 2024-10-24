package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "offering")
data class OfferingEntity(
  @Id
  @GeneratedValue
  @Column(name = "offering_id")
  val id: UUID? = null,

  @Version
  @Column(name = "version", nullable = false)
  val version: Long = 0,

  val organisationId: String,
  var contactEmail: String,
  var secondaryContactEmail: String? = null,
  var withdrawn: Boolean = false,
  var referable: Boolean = true,
) {
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "course_id")
  lateinit var course: CourseEntity
}

data class GenderOffering(val offeringEntity: OfferingEntity, val gender: String)