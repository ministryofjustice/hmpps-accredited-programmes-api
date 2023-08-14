package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Referral(
  @Id
  @GeneratedValue
  @Column(name = "referral_id")
  val id: UUID? = null,

  val offeringId: UUID,
  val prisonNumber: String,
  val referrerId: String,
)
