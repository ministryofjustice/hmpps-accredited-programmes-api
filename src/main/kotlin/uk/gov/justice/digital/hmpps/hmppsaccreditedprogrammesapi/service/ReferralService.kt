package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import jakarta.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ReferralService
@Autowired
constructor(
  private val referralRepository: ReferralRepository,
  private val referrerUserRepository: ReferrerUserRepository,
  private val offeringRepository: JpaOfferingRepository,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val referralSummaryBuilderService: ReferralSummaryBuilderService,
) {

  fun createReferral(
    prisonNumber: String,
    offeringId: UUID,
  ): UUID? {
    val username = SecurityContextHolder.getContext().authentication?.name
      ?: throw SecurityException("Authentication information not found")

    val referrerUser = referrerUserRepository.findById(username).orElseGet {
      referrerUserRepository.save(ReferrerUserEntity(username = username))
    }

    val offering = offeringRepository.findById(offeringId)
      .orElseThrow { Exception("Offering not found") }

    return referralRepository.save(
      ReferralEntity(
        offering = offering,
        prisonNumber = prisonNumber,
        referrer = referrerUser,
      ),
    ).id ?: throw Exception("Referral creation failed")
  }

  fun getReferralById(referralId: UUID): ReferralEntity? = referralRepository.findById(referralId).getOrNull()

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
      referral.offering.id to "offeringId",
      referral.prisonNumber to "prisonNumber",
      referral.referrer to "referrer",
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

  fun getReferralsByOrganisationId(
    organisationId: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralSummary> {
    val statusEnums = status?.map { ReferralEntity.ReferralStatus.valueOf(it) }
    val referralProjectionPage = referralRepository.getReferralsByOrganisationId(organisationId, pageable, statusEnums, audience, courseName)
    val content = referralProjectionPage.content
    val prisonersDetails =
      prisonerSearchApiService.getPrisoners(content.map { it.prisonNumber }.distinct())
    val allPrisons = prisonRegisterApiService.getAllPrisons()

    val apiContent = referralSummaryBuilderService.build(content, prisonersDetails, allPrisons, organisationId)

    return PageImpl(apiContent, pageable, referralProjectionPage.totalElements)
  }
}
