package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.CascadeType.MERGE
import jakarta.persistence.CascadeType.PERSIST
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.util.UUID

@Entity
@Table(name = "course")
class CourseEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "course_id")
  var id: UUID? = null,

  var name: String,
  var type: String,
  var description: String? = null,

  @ManyToMany(cascade = [PERSIST, MERGE])
  @JoinTable(
    name = "course_prerequisite",
    joinColumns = [JoinColumn(name = "course_id")],
    inverseJoinColumns = [JoinColumn(name = "prerequisite_id")],
  )
  var prerequisites: MutableSet<Prerequisite> = mutableSetOf(),

  @Transient
  var audience: Set<Audience> = emptySet(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CourseEntity) return false
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1756406093
}
