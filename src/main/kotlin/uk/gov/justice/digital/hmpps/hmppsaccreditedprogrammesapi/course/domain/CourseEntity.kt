package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode.SUBSELECT
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

  @ElementCollection
  @Fetch(SUBSELECT)
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  val prerequisites: MutableSet<Prerequisite> = mutableSetOf(),

  @ElementCollection
  @Fetch(SUBSELECT)
  @CollectionTable(name = "offering", joinColumns = [JoinColumn(name = "course_id")])
  val offerings: MutableSet<Offering> = mutableSetOf(),

  @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
  @Fetch(SUBSELECT)
  @JoinTable(
    name = "course_audience",
    joinColumns = [JoinColumn(name = "course_id")],
    inverseJoinColumns = [JoinColumn(name = "audience_id")],
  )
  var audiences: MutableSet<Audience> = mutableSetOf(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CourseEntity) return false
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1756406093

  override fun toString(): String = "CourseEntity($name, $description, $audiences, $id)"
}
