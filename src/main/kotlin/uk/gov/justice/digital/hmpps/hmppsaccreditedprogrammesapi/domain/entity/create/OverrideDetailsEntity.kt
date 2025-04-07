package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "override_details")
class OverrideDetailsEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  val id: UUID? = null,

  @Column(name = "recommended_pathway", nullable = true)
  val recommendedPathway: String? = null,

  @Column(name = "requested_pathway", nullable = true)
  val requestedPathway: String? = null,

  @Column(name = "referrer_override_reason", nullable = true)
  val referrerOverrideReason: String? = null,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", referencedColumnName = "referral_id")
  var referral: ReferralEntity? = null
)


