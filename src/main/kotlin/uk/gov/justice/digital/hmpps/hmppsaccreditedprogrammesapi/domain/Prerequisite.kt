package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Prerequisite(
  @Id
  @GeneratedValue
  @Column(name = "prerequisite_id")
  var id: UUID? = null,

  var name: String,
  var description: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Prerequisite) return false
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1756406093
}
