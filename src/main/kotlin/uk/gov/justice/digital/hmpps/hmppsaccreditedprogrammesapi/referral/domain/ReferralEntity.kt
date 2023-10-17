package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralEntity.ReferralStatus.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import java.util.EnumMap
import java.util.EnumSet
import java.util.UUID

@Entity
@Table(name = "referral")
data class ReferralEntity(
  @Id
  @GeneratedValue
  @Column(name = "referral_id")
  val id: UUID? = null,

  val offeringId: UUID,
  val prisonNumber: String,
  val referrerId: String,
  var reason: String? = null,
  var oasysConfirmed: Boolean = false,
  var hasReviewedProgrammeHistory: Boolean = false,
  @Enumerated(STRING)
  var status: ReferralStatus = REFERRAL_STARTED,
) {

  enum class ReferralStatus {
    REFERRAL_STARTED,
    REFERRAL_SUBMITTED,
    AWAITING_ASSESSMENT,
    ASSESSMENT_STARTED,
    ;

    fun isValidTransition(nextReferralStatus: ReferralStatus?): Boolean = validTransitions[this]?.contains(nextReferralStatus) ?: false
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
