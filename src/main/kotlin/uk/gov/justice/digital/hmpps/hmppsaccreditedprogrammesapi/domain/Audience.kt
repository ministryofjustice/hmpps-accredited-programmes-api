package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Audience(
  @Column(name = "audience_value")
  var value: String,

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "audience_id")
  var id: UUID? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Audience) return false
    return this.value == other.value
  }

  override fun hashCode(): Int = 1756406093
}
