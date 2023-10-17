package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.repositories.JpaReferralRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ReferralService
@Autowired
constructor (
  private val referralRepository: JpaReferralRepository,
) {
  fun startReferral(
    prisonNumber: String,
    offeringId: UUID,
    referrerId: String,
  ): UUID? = referralRepository.save(Referral(offeringId = offeringId, prisonNumber = prisonNumber, referrerId = referrerId)).id

  fun getReferralById(referralId: UUID) = referralRepository.findById(referralId).getOrNull()

  fun updateReferralById(referralId: UUID, reason: String?, oasysConfirmed: Boolean, hasReviewedProgrammeHistory: Boolean) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.reason = reason
    referral.oasysConfirmed = oasysConfirmed
    referral.hasReviewedProgrammeHistory = hasReviewedProgrammeHistory
  }

  fun updateReferralStatusById(referralId: UUID, nextStatus: Status) {
    val referral = referralRepository.getReferenceById(referralId)
    if (referral.status.isValidTransition(nextStatus)) {
      referral.status = nextStatus
    } else {
      throw IllegalArgumentException("Transition from ${referral.status} to $nextStatus is not valid")
    }
  }
}
