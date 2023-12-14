package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import java.time.LocalDateTime
import java.util.EnumMap
import java.util.EnumSet
import java.util.UUID

@Entity
@Table(name = "referral")
data class ReferralEntity(
  @Id
  @GeneratedValue
  @Column(name = "referral_id")
  var id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offering_id", referencedColumnName = "offering_id")
  var offering: OfferingEntity,

  val prisonNumber: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referrer_username", referencedColumnName = "referrer_username")
  var referrer: ReferrerUserEntity,

  var additionalInformation: String? = null,

  var oasysConfirmed: Boolean = false,

  var hasReviewedProgrammeHistory: Boolean = false,

  @Enumerated(STRING)
  var status: ReferralStatus = REFERRAL_STARTED,

  var submittedOn: LocalDateTime? = null,
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
