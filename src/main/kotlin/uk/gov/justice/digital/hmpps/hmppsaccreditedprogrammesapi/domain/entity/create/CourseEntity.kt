package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode.SUBSELECT
import org.hibernate.annotations.Immutable
import java.util.UUID

@Entity
@Table(name = "course")
data class CourseEntity(
  @Id
  @GeneratedValue
  @Column(name = "course_id")
  val id: UUID? = null,

  var name: String,
  var identifier: String,
  var description: String? = null,
  var alternateName: String? = null,
  var referable: Boolean = true,

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(SUBSELECT)
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  val prerequisites: MutableSet<PrerequisiteEntity> = mutableSetOf(),

  @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
  @Column(name = "offerings")
  @Fetch(SUBSELECT)
  val offerings: MutableSet<OfferingEntity> = mutableSetOf(),

  @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
  @Fetch(SUBSELECT)
  @JoinTable(
    name = "course_audience",
    joinColumns = [JoinColumn(name = "course_id")],
    inverseJoinColumns = [JoinColumn(name = "audience_id")],
  )
  var audiences: MutableSet<AudienceEntity> = mutableSetOf(),
  var withdrawn: Boolean = false,
) {
  fun addOffering(offering: OfferingEntity) {
    offering.course = this
    offerings += offering
  }
}

@Embeddable
@Immutable
data class PrerequisiteEntity(
  val name: String,
  val description: String,
)
