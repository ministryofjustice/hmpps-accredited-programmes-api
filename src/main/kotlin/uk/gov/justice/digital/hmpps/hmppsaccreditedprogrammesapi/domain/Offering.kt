package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class Offering(
  val organisationId: String,
  var contactEmail: String,
  var secondaryContactEmail: String? = null,

  @Column(name = "offering_id")
  val id: UUID = UUID.randomUUID(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is Offering) return false
    return organisationId == other.organisationId
  }

  override fun hashCode(): Int = organisationId.hashCode()
}
