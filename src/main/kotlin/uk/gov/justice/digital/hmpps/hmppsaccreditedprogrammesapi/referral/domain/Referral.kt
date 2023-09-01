package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_SUBMITTED
import java.util.EnumMap
import java.util.EnumSet
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
  var status: Status = REFERRAL_STARTED,
) {

  enum class Status {
    REFERRAL_STARTED,
    REFERRAL_SUBMITTED,
    AWAITING_ASSESSMENT,
    ASSESSMENT_STARTED,
    ;

    fun isValidTransition(nextStatus: Status?): Boolean = validTransitions[this]?.contains(nextStatus) ?: false
  }

  companion object {
    private val validTransitions = EnumMap(
      mapOf(
        REFERRAL_STARTED to EnumSet.of(REFERRAL_STARTED, REFERRAL_SUBMITTED),
        REFERRAL_SUBMITTED to EnumSet.of(REFERRAL_SUBMITTED, AWAITING_ASSESSMENT),
        AWAITING_ASSESSMENT to EnumSet.of(AWAITING_ASSESSMENT, ASSESSMENT_STARTED),
        ASSESSMENT_STARTED to EnumSet.of(ASSESSMENT_STARTED),
      ),
    )
  }
}
