package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaReferralRepository
import java.time.LocalDateTime
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

  fun submitReferralById(referralId: UUID) {
    val referral = referralRepository.getReferenceById(referralId)

    val requiredFields = listOf(
      referral.offeringId to "offeringId",
      referral.prisonNumber to "prisonNumber",
      referral.referrerId to "referrerId",
      referral.additionalInformation to "additionalInformation",
      referral.oasysConfirmed to "oasysConfirmed",
      referral.hasReviewedProgrammeHistory to "hasReviewedProgrammeHistory",
    )

    for ((value, fieldName) in requiredFields) {
      when (value) {
        null -> throw ValidationException("$fieldName is not valid: null")
        is String -> if (value.isBlank()) throw ValidationException("$fieldName is not valid: blank")
      }
    }

    when (referral.status) {
      ReferralStatus.REFERRAL_STARTED -> {
        referral.status = ReferralStatus.REFERRAL_SUBMITTED
        referral.submittedOn = LocalDateTime.now()
      }
      ReferralStatus.REFERRAL_SUBMITTED -> {
        throw IllegalArgumentException("Referral $referralId is already submitted")
      }
      ReferralStatus.AWAITING_ASSESSMENT -> {
        throw IllegalArgumentException("Referral $referralId is already submitted and awaiting assessment")
      }
      ReferralStatus.ASSESSMENT_STARTED -> {
        throw IllegalArgumentException("Referral $referralId is already submitted and currently being assessed")
      }
    }
  }

  fun getReferralSummaryByOrgId(orgId: String) = referralRepository.getReferralsByOrgId(orgId)
}
