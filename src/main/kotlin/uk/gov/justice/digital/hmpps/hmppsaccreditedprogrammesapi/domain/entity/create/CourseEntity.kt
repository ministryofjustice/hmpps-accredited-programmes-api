package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode.SUBSELECT
import org.hibernate.annotations.Immutable
import java.util.UUID

@Entity
@Table(name = "course")
class CourseEntity(
  @Id
  @Column(name = "course_id")
  val id: UUID? = null,

  var name: String,
  var identifier: String,
  var description: String? = null,
  var alternateName: String? = null,

  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(SUBSELECT)
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  @OrderBy("description DESC")
  val prerequisites: MutableSet<PrerequisiteEntity> = mutableSetOf(),

  @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
  @Fetch(SUBSELECT)
  val offerings: MutableSet<OfferingEntity> = mutableSetOf(),

  var audience: String,
  var audienceColour: String?,
  var withdrawn: Boolean = false,
  var listDisplayName: String? = null,
  var displayOnProgrammeDirectory: Boolean? = true,
  var intensity: String? = null,
) {
  fun updatePrerequisites(newPrerequisites: Set<PrerequisiteEntity>) {
    this.prerequisites.clear()
    this.prerequisites.addAll(newPrerequisites)
  }

  override fun toString(): String = "CourseEntity(id=$id, name='$name', identifier='$identifier', description=$description, alternateName=$alternateName, prerequisites=$prerequisites, offerings=${offerings.map { it.id }}, audience='$audience', audienceColour=$audienceColour, withdrawn=$withdrawn, listDisplayName=$listDisplayName, displayOnProgrammeDirectory=$displayOnProgrammeDirectory, intensity=$intensity)"
}

@Embeddable
@Immutable
data class PrerequisiteEntity(
  val name: String,
  val description: String,
)
