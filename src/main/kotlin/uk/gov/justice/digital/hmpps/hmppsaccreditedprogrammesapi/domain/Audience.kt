package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import java.util.UUID

class Audience(
  val value: String,
  val id: UUID? = UUID.randomUUID(),
) : CharSequence by value {
  init {
    require(value.isNotBlank())
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Audience) return false
    return this.value == other.value
  }

  override fun hashCode(): Int = value.hashCode()
}
