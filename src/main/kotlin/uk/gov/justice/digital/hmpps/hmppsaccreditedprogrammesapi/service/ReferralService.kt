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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import java.math.BigInteger
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
  private val referralViewRepository: ReferralViewRepository,
  private val referralStatusHistoryService: ReferralStatusHistoryService,
  private val auditService: AuditService,
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusCategoryRepository: ReferralStatusCategoryRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
  private val referralReferenceDataService: ReferralReferenceDataService,
  private val enabledOrganisationService: EnabledOrganisationService,
  private val personService: PersonService,
  private val pniService: PniService,
  private val caseNotesApiService: CaseNotesApiService,
  private val organisationService: OrganisationService,
  private val staffService: StaffService,
  private val courseParticipationService: CourseParticipationService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun createReferral(
    prisonNumber: String,
    offeringId: UUID,
  ): Referral {
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

    personService.createOrUpdatePerson(prisonNumber)

    organisationService.createOrganisationIfNotPresent(offering.organisationId)

    val savedReferral = referralRepository.save(
      ReferralEntity(
        offering = offering,
        prisonNumber = prisonNumber,
        referrer = referrerUser,
      ),
    )
      ?: throw Exception("Referral creation failed for $prisonNumber").also { log.warn("Failed to create referral for $prisonNumber") }

    referralStatusHistoryService.createReferralHistory(savedReferral)
    auditService.audit(savedReferral, null, AuditAction.CREATE_REFERRAL.name)

    log.info("FINISHED - Request processed successfully to create a referral for prisonNumber $prisonNumber from $username referralId: ${savedReferral.id}")
    return savedReferral.toApi()
  }

  private fun savePNI(savedReferral: ReferralEntity) {
    try {
      pniService.savePni(prisonNumber = savedReferral.prisonNumber, gender = null, savePni = true, referralId = savedReferral.id)
    } catch (ex: Exception) {
      log.warn("PNI could not be stored ${ex.message} for prisonNumber $savedReferral.prisonNumber")
    }
  }

  fun getReferralById(referralId: UUID) =
    referralRepository.findById(referralId).getOrNull()

  fun updateReferralById(referralId: UUID, update: ReferralUpdate) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.additionalInformation = update.additionalInformation
    referral.oasysConfirmed = update.oasysConfirmed
    referral.hasReviewedProgrammeHistory = update.hasReviewedProgrammeHistory
  }

  fun updateReferralStatusById(referralId: UUID, referralStatusUpdate: ReferralStatusUpdate) {
    log.info("Request received for update of status for $referralId and $referralStatusUpdate")
    val referral = referralRepository.getReferenceById(referralId)
    val statuses = validateStatus(referral, referralStatusUpdate)
    val existingStatus = referral.status

    if (isSpecialDeselectedCase(referralId, statuses, existingStatus, referralStatusUpdate)) {
      val updatedReferral = referralRepository.getReferenceById(referralId)
      // create the referral history
      referralStatusHistoryService.updateReferralHistory(
        referralId = referralId,
        previousStatusCode = updatedReferral.status,
        newStatus = statuses.first,
      )
    } else {
      // create the referral history
      referralStatusHistoryService.updateReferralHistory(
        referralId = referralId,
        previousStatusCode = existingStatus,
        newStatus = statuses.first,
        newCategory = statuses.second,
        newReason = statuses.third,
        newNotes = referralStatusUpdate.notes,
      )
    }
    // update the status
    referral.status = referralStatusUpdate.status.uppercase()

    // write case notes
    caseNotesApiService.buildAndCreateCaseNote(referral, referralStatusUpdate)

    // save PNI when referral is updated to "On Programme" status
    if (referral.status == "ON_PROGRAMME") {
      savePNI(referral)
    }

    // audit the interaction
    auditService.audit(referral, existingStatus, AuditAction.UPDATE_REFERRAL.name)
  }

  /**
   *  This is a special case where the current status is ON_PROGRAMME and they are moving to a
   *  status that is not closed we need to insert a deselected status here.
   *  This is unfortunate as it breaks the configuration of the status transitions [sad face]
   */
  private fun isSpecialDeselectedCase(
    referralId: UUID,
    status: Triple<ReferralStatusEntity, ReferralStatusCategoryEntity?, ReferralStatusReasonEntity?>,
    existingStatus: String,
    referralStatusUpdate: ReferralStatusUpdate,
  ): Boolean {
    if (existingStatus == "ON_PROGRAMME" && !status.first.closed) {
      val newStatus = referralStatusRepository.getByCode("DESELECTED")
      referralStatusHistoryService.updateReferralHistory(
        referralId = referralId,
        previousStatusCode = existingStatus,
        newStatus = newStatus,
        newCategory = status.second,
        newReason = status.third,
        newNotes = referralStatusUpdate.notes,
      )
      return true
    }
    return false
  }

  private fun validateStatus(
    referral: ReferralEntity,
    referralStatusUpdate: ReferralStatusUpdate,
  ): Triple<ReferralStatusEntity, ReferralStatusCategoryEntity?, ReferralStatusReasonEntity?> {
    // validate that the status exists:
    val status = referralStatusRepository.getByCode(referralStatusUpdate.status.uppercase())
    val category = referralStatusUpdate.category?.uppercase()?.let { referralStatusCategoryRepository.getByCode(it) }
    val reason = referralStatusUpdate.reason?.uppercase()?.let { referralStatusReasonRepository.getByCode(it) }

    validateStatusTransition(
      referral.id!!,
      referral.status,
      referralStatusUpdate.status.uppercase(),
      referralStatusUpdate.ptUser!!,
    )

    return Triple(status, category, reason)
  }

  fun submitReferralById(referralId: UUID): ReferralEntity {
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
        fetchAndSavePomDetails(referral).let {
          referral.primaryPomStaffId = it?.first
          referral.secondaryPomStaffId = it?.second
        }
        caseNotesApiService.buildAndCreateCaseNote(referral, ReferralStatusUpdate(status = "REFERRAL_SUBMITTED"))
        courseParticipationService.updateDraftHistoryForSubmittedReferral(referralId)
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
    return referral
  }

  fun getReferralViewByOrganisationId(
    organisationId: String,
    prisonNumber: String?,
    surnameOnly: String?,
    forename: String?,
    surname: String?,
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
        prisonNumber,
        surnameOnly,
        forename,
        surname,
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
    // Convert existing status to uppercase, or initialize as an empty list if status is null
    val uppercaseStatuses = status?.map { it.uppercase() }?.toMutableList() ?: mutableListOf()

    // Retrieve the statuses for the specified group
    val groupStatuses = statusGroup?.let { group ->
      when (group) {
        "closed" -> referralStatusRepository.findAllByActiveIsTrueAndClosedIsTrueOrderByDefaultOrder().map { it.code }
        "draft" -> referralStatusRepository.findAllByActiveIsTrueAndDraftIsTrueOrderByDefaultOrder().map { it.code }
        "open" -> referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder().map { it.code }
        else -> emptyList()
      }
    } ?: emptyList()

    // If both status and statusGroup are provided, filter statuses by the group statuses
    val filteredStatuses = if (status != null && statusGroup != null) {
      val intersection = uppercaseStatuses.intersect(groupStatuses.toSet()).toList()
      intersection.ifEmpty {
        listOf("INVALID_STATUS")
      }
    } else {
      uppercaseStatuses.apply { addAll(groupStatuses) }
    }

    return filteredStatuses.takeIf { it.isNotEmpty() }
  }

  fun getReferralViewByUsername(
    username: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
    statusGroup: String?,
    prisonNumber: String?,
    surnameOnly: String?,
    forename: String?,
    surname: String?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = getFilterStatuses(status, statusGroup)
    val referralViewPage =
      referralViewRepository.getReferralsByUsername(
        prisonNumber,
        surnameOnly,
        forename,
        surname,
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
      log.error("Cannot transition referral $referralId from $currentStatus to $newStatus")
      throw BusinessException("Cannot transition referral $referralId from $currentStatus to $newStatus")
    }
  }

  fun deleteReferral(referralId: UUID) {
    referralRepository.findById(referralId).ifPresent { referral ->
      val updatedReferral = referral.copy(deleted = true)
      referralRepository.save(updatedReferral)
    }
  }

  fun getDuplicateReferrals(offeringId: UUID, prisonNumber: String): List<ReferralEntity>? {
    val openReferralStatuses =
      referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder().map { it.code }

    return referralRepository.getReferralEntitiesByOfferingIdAndPrisonNumberAndStatusIn(
      offeringId,
      prisonNumber,
      openReferralStatuses,
    )?.filterNot { it.status == "REFERRAL_STARTED" }
  }

  fun fetchAndSavePomDetails(submittedReferral: ReferralEntity): Pair<BigInteger?, BigInteger?> {
    return try {
      val (primaryPom, secondaryPom) = staffService.getOffenderAllocation(submittedReferral.prisonNumber)
      Pair(primaryPom?.staffId, secondaryPom?.staffId)
    } catch (ex: Exception) {
      log.error("Error fetching POM details for prison number ${submittedReferral.prisonNumber}: ${ex.message}", ex)
      Pair(null, null)
    }
  }

  fun getPrisonIdsWithNoPrimaryPom() =
    referralRepository.findAllDistinctPrisonNumbersWithoutPrimaryPom()

  fun updatePoms(it: String, primaryPom: StaffEntity?, secondaryPom: StaffEntity?) {
    val referrals = referralRepository.findAllByPrisonNumber(it)
    val updatedReferrals = mutableListOf<ReferralEntity>()
    log.info("Fetched ${referrals.size} referrals for prisoner $it referralIds ${referrals.map { it.id }}. Start updating referrals with primary pom ${primaryPom?.staffId} and secondary POM ${secondaryPom?.staffId} ")
    referrals.forEach { referral ->
      referral.primaryPomStaffId = primaryPom?.staffId
      referral.secondaryPomStaffId = secondaryPom?.staffId
      updatedReferrals.add(referral)
      log.info("Referral ${referral.id}  for prisoner $it marked for update")
    }

    val savedReferrals = referralRepository.saveAll(updatedReferrals)
    log.info("Update successful for ${referrals.size} referrals for prisoner $it referralIds ${savedReferrals.map { it.id }} Finished updating referrals with primary pom ${primaryPom?.staffId} and secondary POM ${secondaryPom?.staffId} ")
  }
}
