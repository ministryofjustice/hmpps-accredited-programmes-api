package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode.SUBSELECT
import org.hibernate.annotations.Immutable
import java.util.UUID

@Entity
@Table(name = "course")
class CourseEntity(
  @Id
  @GeneratedValue
  @Column(name = "course_id")
  val id: UUID? = null,

  var name: String,
  var identifier: String,
  var description: String? = null,
  var alternateName: String? = null,
  var referable: Boolean = true,

  @ElementCollection
  @Fetch(SUBSELECT)
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  val prerequisites: MutableSet<Prerequisite> = mutableSetOf(),

  @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
  @Column(name = "offerings")
  @Fetch(SUBSELECT)
  private val mutableOfferings: MutableSet<OfferingEntity> = mutableSetOf(),

  @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
  @Fetch(SUBSELECT)
  @JoinTable(
    name = "course_audience",
    joinColumns = [JoinColumn(name = "course_id")],
    inverseJoinColumns = [JoinColumn(name = "audience_id")],
  )
  var audiences: MutableSet<AudienceEntity> = mutableSetOf(),
  var withdrawn: Boolean = false,
) {
  @get:Transient
  val offerings: Set<OfferingEntity>
    get() = mutableOfferings

  fun addOffering(offering: OfferingEntity) {
    offering.course = this
    mutableOfferings += offering
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CourseEntity) return false
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1756406093

  override fun toString(): String = "CourseEntity($name, $description, $audiences, $id)"
}

@Embeddable
@Immutable
data class Prerequisite(
  val name: String,
  val description: String,
)
