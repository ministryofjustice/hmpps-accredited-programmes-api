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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
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
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusCategoryRepository: ReferralStatusCategoryRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
  private val referralReferenceDataService: ReferralReferenceDataService,
  private val enabledOrganisationService: EnabledOrganisationService,
  private val personService: PersonService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  fun createReferral(
    prisonNumber: String,
    offeringId: UUID,
  ): UUID? {
    val username = SecurityContextHolder.getContext().authentication?.name
      ?: throw SecurityException("Authentication information not found")
    log.info("STARTING - Request received to create a referral for prisonNumber $prisonNumber from $username")

    val referrerUser = referrerUserRepository.findById(username).orElseGet {
      referrerUserRepository.save(ReferrerUserEntity(username = username))
    }

    val offering = offeringRepository.findById(offeringId)
      .orElseThrow { Exception("Offering not found for $offeringId") }

    if (enabledOrganisationService.getEnabledOrganisation(offering.organisationId) == null) {
      throw BusinessException("Organisation ${offering.organisationId} not enabled for referrals")
    }

    createOrUpdatePerson(prisonNumber)

    createOrganisationIfNotPresent(offering.organisationId)

    val savedReferral = referralRepository.save(
      ReferralEntity(
        offering = offering,
        prisonNumber = prisonNumber,
        referrer = referrerUser,
      ),
    ) ?: throw Exception("Referral creation failed for $prisonNumber").also { log.warn("Failed to create referral for $prisonNumber") }

    referralStatusHistoryService.createReferralHistory(savedReferral)
    auditService.audit(savedReferral, null, AuditAction.CREATE_REFERRAL.name)
    log.info("FINISHED - Request processed successfully to create a referral for prisonNumber $prisonNumber from $username referralId: ${savedReferral.id}")
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
          throw BusinessException("Failed to save organisation details for prison $code", e)
        }
      } ?: {
        log.warn("Prison details could not be fetched for $code")
        throw BusinessException("Prison details could not be fetched for $code")
      }
    }
  }

  fun updatePerson(prisonNumber: String) {
    log.info("Attempting to update person with prison number: $prisonNumber")
    val personEntity = personRepository.findPersonEntityByPrisonNumber(prisonNumber)
    if (personEntity != null) {
      log.info("Prisoner is of interest to ACP - about to update: $prisonNumber")
      val sentenceType = personService.getSentenceType(prisonNumber)
      prisonerSearchApiService.getPrisoners(listOf(prisonNumber)).firstOrNull()?.let {
        updatePerson(it, personEntity, sentenceType)
      }
      personRepository.save(personEntity)
    } else {
      log.info("Prisoner is not of interest to ACP")
    }
  }

  private fun updatePerson(
    it: Prisoner,
    personEntity: PersonEntity,
    sentenceType: String,
  ) {
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
    personEntity.sentenceType = sentenceType
  }

  private fun createOrUpdatePerson(prisonNumber: String) {
    val sentenceType = personService.getSentenceType(prisonNumber)
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
          sentenceType,
        )
      } else {
        updatePerson(it, personEntity, sentenceType)
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

  fun updateReferralStatusById(referralId: UUID, referralStatusUpdate: ReferralStatusUpdate) {
    val referral = referralRepository.getReferenceById(referralId)
    val statuses = validateStatus(referral, referralStatusUpdate)
    val existingStatus = referral.status
    // create the referral history
    referralStatusHistoryService.updateReferralHistory(
      referralId = referralId,
      previousStatusCode = existingStatus,
      newStatus = statuses.first,
      newCategory = statuses.second,
      newReason = statuses.third,
      newNotes = referralStatusUpdate.notes,
    )
    // update the status
    referral.status = referralStatusUpdate.status
    // audit the interaction
    auditService.audit(referral, existingStatus, AuditAction.UPDATE_REFERRAL.name)
  }

  private fun validateStatus(
    referral: ReferralEntity,
    referralStatusUpdate: ReferralStatusUpdate,
  ): Triple<ReferralStatusEntity, ReferralStatusCategoryEntity?, ReferralStatusReasonEntity?> {
    // validate that the status exists:
    val status = referralStatusRepository.getByCode(referralStatusUpdate.status.uppercase())
    val category = referralStatusUpdate.category?.uppercase()?.let { referralStatusCategoryRepository.getByCode(it) }
    val reason = referralStatusUpdate.reason?.uppercase()?.let { referralStatusReasonRepository.getByCode(it) }

    validateStatusTransition(referral.id!!, referral.status, referralStatusUpdate.status.uppercase(), referralStatusUpdate.ptUser!!)

    return Triple(status, category, reason)
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

    referralStatusHistoryService.updateReferralHistory(
      referralId = referralId,
      previousStatusCode = existingStatus,
      newStatus = referralStatusRepository.getByCode(referral.status),
    )
  }

  fun getReferralViewByOrganisationId(
    organisationId: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
    statusGroup: String?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = getFilterStatuses(status, statusGroup)

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

  private fun getFilterStatuses(
    status: List<String>?,
    statusGroup: String?,
  ): List<String>? {
    var uppercaseStatuses = status?.map { it.uppercase() }
    if (uppercaseStatuses == null || (uppercaseStatuses.isEmpty() && statusGroup != null)) {
      uppercaseStatuses = when (statusGroup) {
        "closed" -> {
          referralStatusRepository.findAllByActiveIsTrueAndClosedIsTrue().map { it.code }
        }

        "draft" -> {
          referralStatusRepository.findAllByActiveIsTrueAndDraftIsTrue().map { it.code }
        }

        "open" -> {
          referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalse().map { it.code }
        }

        else -> {
          null
        }
      }
    }
    return uppercaseStatuses
  }

  fun getReferralViewByUsername(
    username: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
    statusGroup: String?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = getFilterStatuses(status, statusGroup)
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

  fun validateStatusTransition(referralId: UUID, currentStatus: String, newStatus: String, ptUser: Boolean) {
    val validTransitions = referralReferenceDataService.getNextStatusTransitions(currentStatus, ptUser)
    if (validTransitions.none { it.code == newStatus }) {
      throw BusinessException("Cannot transition referral $referralId from $currentStatus to $newStatus")
    }
  }
}
