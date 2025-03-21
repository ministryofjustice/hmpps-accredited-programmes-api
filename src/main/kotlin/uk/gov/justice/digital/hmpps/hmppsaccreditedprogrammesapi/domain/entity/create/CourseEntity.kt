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
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.Version
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
  var id: UUID? = null,

  @Version
  @Column(name = "version", nullable = false)
  var version: Long = 0,

  var name: String,
  var identifier: String,
  var description: String? = null,
  var alternateName: String? = null,

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(SUBSELECT)
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  @OrderBy("description DESC")
  var prerequisites: MutableSet<PrerequisiteEntity> = mutableSetOf(),

  @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
  var offerings: MutableSet<OfferingEntity> = mutableSetOf(),

  var audience: String,
  var audienceColour: String?,
  var withdrawn: Boolean = false,
  var listDisplayName: String? = null,
  var displayOnProgrammeDirectory: Boolean? = true,
  var intensity: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as CourseEntity
    return this.id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}

@Embeddable
@Immutable
class PrerequisiteEntity(
  val name: String,
  val description: String,
)
