package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_STARTED
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
  var reason: String? = null,
  var oasysConfirmed: Boolean = false,
  @Enumerated(STRING)
  val status: Status = REFERRAL_STARTED,
) {

  enum class Status {
    REFERRAL_STARTED, REFERRAL_SUBMITTED, AWAITING_ASSESSMENT, ASSESSMENT_STARTED,
  }
}
