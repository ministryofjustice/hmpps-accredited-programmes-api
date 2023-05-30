package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import jakarta.persistence.Transient
import java.util.*

@Entity
@Table(name = "course")
class CourseEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "course_id")
  val id: UUID? = null,

  var name: String,
  var type: String,
  var description: String? = null,

  @ElementCollection
  @CollectionTable(name = "prerequisite", joinColumns = [JoinColumn(name = "course_id")])
  val prerequisites: MutableSet<Prerequisite> = mutableSetOf(),

  @Transient
  var audiences: List<Audience> = emptyList(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CourseEntity) return false
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1756406093
}
