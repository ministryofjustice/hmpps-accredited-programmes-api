package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import java.time.LocalDate
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
  private val offeringRepository: OfferingRepository,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val personRepository: PersonRepository,
  private val organisationRepository: OrganisationRepository,
  private val referralViewRepository: ReferralViewRepository,
  private val referralStatusHistoryService: ReferralStatusHistoryService,
  private val auditService: AuditService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
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

    createOrUpdatePerson(prisonNumber)

    createOrganisationIfNotPresent(offering.organisationId)

    val savedReferral = referralRepository.save(
      ReferralEntity(
        offering = offering,
        prisonNumber = prisonNumber,
        referrer = referrerUser,
      ),
    ) ?: throw Exception("Referral creation failed")

    referralStatusHistoryService.createReferralHistory(savedReferral)
    auditService.createAuditRecord(savedReferral, null)
    return savedReferral.id
  }

  private fun createOrganisationIfNotPresent(code: String) {
    val organisation = organisationRepository.findOrganisationEntityByCode(code)

    if (organisation == null) {
      prisonRegisterApiService.getPrisonById(code)?.let {
        try {
          organisationRepository.save(OrganisationEntity(code = it.prisonId, name = it.prisonName))
        } catch (e: Exception) {
          log.warn("Failed to save organisation details for prison $code", e)
        }
      } ?: log.warn("Prison details could not be fetched for $code")
    }
  }

  private fun createOrUpdatePerson(prisonNumber: String) {
    prisonerSearchApiService.getPrisoners(listOf(prisonNumber)).firstOrNull()?.let {
      var personEntity = personRepository.findPersonEntityByPrisonNumber(prisonNumber)
      if (personEntity == null) {
        val earliestReleaseDateAndType = earliestReleaseDateAndType(it)
        personEntity = PersonEntity(
          it.lastName,
          it.firstName,
          prisonNumber,
          it.conditionalReleaseDate,
          it.paroleEligibilityDate,
          it.tariffDate,
          earliestReleaseDateAndType.first,
          earliestReleaseDateAndType.second,
          it.indeterminateSentence,
          it.nonDtoReleaseDateType,
        )
      } else {
        val earliestReleaseDateAndType = earliestReleaseDateAndType(it)
        personEntity.surname = it.lastName
        personEntity.forename = it.firstName
        personEntity.conditionalReleaseDate = it.conditionalReleaseDate
        personEntity.paroleEligibilityDate = it.paroleEligibilityDate
        personEntity.tariffExpiryDate = it.tariffDate
        personEntity.earliestReleaseDate = earliestReleaseDateAndType.first
        personEntity.earliestReleaseDateType = earliestReleaseDateAndType.second
        personEntity.indeterminateSentence = it.indeterminateSentence
        personEntity.nonDtoReleaseDateType = it.nonDtoReleaseDateType
      }
      personRepository.save(personEntity)
    }
  }

  private fun earliestReleaseDateAndType(prisoner: Prisoner): Pair<LocalDate?, String?> {
    return when {
      prisoner.indeterminateSentence == true -> Pair(prisoner.tariffDate, "Tariff Date")
      prisoner.paroleEligibilityDate != null -> Pair(prisoner.paroleEligibilityDate, "Parole Eligibility Date")
      prisoner.conditionalReleaseDate != null -> Pair(prisoner.conditionalReleaseDate, "Conditional Release Date")
      else -> Pair(null, null)
    }
  }

  fun getReferralById(referralId: UUID, updatePersonDetails: Boolean = false): ReferralEntity? {
    val referral = referralRepository.findById(referralId).getOrNull()

    if (referral != null && updatePersonDetails) {
      createOrUpdatePerson(referral.prisonNumber)
      createOrganisationIfNotPresent(referral.offering.organisationId)
    }
    return referral
  }

  fun updateReferralById(referralId: UUID, update: ReferralUpdate) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.additionalInformation = update.additionalInformation
    referral.oasysConfirmed = update.oasysConfirmed
    referral.hasReviewedProgrammeHistory = update.hasReviewedProgrammeHistory
  }

  fun updateReferralStatusById(referralId: UUID, nextStatus: String) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.status = nextStatus
  }

  fun submitReferralById(referralId: UUID) {
    val referral = referralRepository.getReferenceById(referralId)
    val existingStatus = referral.status

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
      "REFERRAL_STARTED" -> {
        referral.status = "REFERRAL_SUBMITTED"
        referral.submittedOn = LocalDateTime.now()
      }

      "REFERRAL_SUBMITTED" -> {
        throw IllegalArgumentException("Referral $referralId is already submitted")
      }

      "AWAITING_ASSESSMENT" -> {
        throw IllegalArgumentException("Referral $referralId is already submitted and awaiting assessment")
      }

      "ASSESSMENT_STARTED" -> {
        throw IllegalArgumentException("Referral $referralId is already submitted and currently being assessed")
      }
    }

    referralStatusHistoryService.updateReferralHistory(referral, existingStatus)
  }

  fun getReferralViewByOrganisationId(
    organisationId: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = status?.map { it.uppercase() }
    val referralViewPage =
      referralViewRepository.getReferralsByOrganisationId(
        organisationId,
        pageable,
        uppercaseStatuses,
        audience,
        courseName,
      )

    return PageImpl(referralViewPage.content, pageable, referralViewPage.totalElements)
  }

  fun getReferralViewByUsername(
    username: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = status?.map { it.uppercase() }
    val referralViewPage =
      referralViewRepository.getReferralsByUsername(
        username,
        pageable,
        uppercaseStatuses,
        audience,
        courseName,
      )

    return PageImpl(referralViewPage.content, pageable, referralViewPage.totalElements)
  }
}
