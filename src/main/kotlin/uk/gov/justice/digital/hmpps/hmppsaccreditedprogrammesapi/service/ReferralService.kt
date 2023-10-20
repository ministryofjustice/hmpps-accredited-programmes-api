package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaReferralRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ReferralService
@Autowired
constructor(
  private val referralRepository: JpaReferralRepository,
) {
  fun createReferral(
    prisonNumber: String,
    offeringId: UUID,
    referrerId: String,
  ): UUID? = referralRepository.save(ReferralEntity(offeringId = offeringId, prisonNumber = prisonNumber, referrerId = referrerId)).id

  fun getReferralById(referralId: UUID) = referralRepository.findById(referralId).getOrNull()

  fun updateReferralById(referralId: UUID, update: ReferralUpdate) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.reason = update.reason
    referral.additionalInformation = update.additionalInformation
    referral.oasysConfirmed = update.oasysConfirmed
    referral.hasReviewedProgrammeHistory = update.hasReviewedProgrammeHistory
  }

  fun updateReferralStatusById(referralId: UUID, nextStatus: ReferralStatus) {
    val referral = referralRepository.getReferenceById(referralId)
    if (referral.status.isValidTransition(nextStatus)) {
      referral.status = nextStatus
    } else {
      throw IllegalArgumentException("Transition from ${referral.status} to $nextStatus is not valid")
    }
  }
}
