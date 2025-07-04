package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EligibilityOverrideReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OverrideType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.EligibilityOverrideReasonEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SelectedSexualOffenceDetailsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SexualOffenceDetailsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.TransferReferralRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

private const val MINIMUM_HSP_OFFENCE_SCORE_TOTAL = 3

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
  private val personService: PersonService,
  private val pniService: PniService,
  private val caseNotesApiService: CaseNotesApiService,
  private val organisationService: OrganisationService,
  private val staffService: StaffService,
  private val courseParticipationService: CourseParticipationService,
  private val referenceDataService: ReferralReferenceDataService,
  @Value("\${spring.application.environment}") val environment: String,
  private val selectedSexualOffenceDetailsRepository: SelectedSexualOffenceDetailsRepository,
  private val sexualOffenceDetailsRepository: SexualOffenceDetailsRepository,
  private val eligibilityOverrideReasonEntityRepository: EligibilityOverrideReasonEntityRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun createHspReferral(
    prisonNumber: String,
    offeringId: UUID,
    selectedOffenceIds: List<UUID>,
    eligibilityOverrideReason: String? = null,
  ): Referral {
    log.info("STARTING - Request received to create an HSP referral for prisonNumber $prisonNumber")

    // validate that the sum of selected offence scores meets the HSP eligibility threshold
    val sumOfSelectedOffenceScores = calculateTotalOffenceScore(selectedOffenceIds)
    if (sumOfSelectedOffenceScores < MINIMUM_HSP_OFFENCE_SCORE_TOTAL && eligibilityOverrideReason == null) {
      log.warn("Request received to create an HSP referral for prisonNumber $prisonNumber but the sum of selected offence scores is $sumOfSelectedOffenceScores which is less than the minimum threshold of $MINIMUM_HSP_OFFENCE_SCORE_TOTAL and no override reason has been provided.")
      throw ValidationException("The sum of selected offence scores is less than the minimum threshold of $MINIMUM_HSP_OFFENCE_SCORE_TOTAL and no override reason has been provided.")
    }

    val savedReferral = createReferral(prisonNumber, offeringId)

    // persist selected offence details and associate with referral
    val savedSelectedOffences = selectedOffenceIds
      .map { createSelectedSexualOffenceDetailsEntity(savedReferral, it) }
      .map { selectedSexualOffenceDetailsRepository.save(it) }
    savedReferral.selectedSexualOffenceDetails = savedSelectedOffences.toMutableSet()

    // persist override reasons if present, and associate with referral
    eligibilityOverrideReason?.let { reasonText ->
      val savedOverrideReason = eligibilityOverrideReasonEntityRepository.save(
        EligibilityOverrideReasonEntity(
          referral = savedReferral,
          reason = reasonText,
          overrideType = OverrideType.HEALTHY_SEX_PROGRAMME,
        ),
      )
      savedReferral.eligibilityOverrideReasons = mutableSetOf(savedOverrideReason)
    }
    log.info("FINISHED - Request processed successfully to create an HSP referral for prisonNumber $prisonNumber with referralId: ${savedReferral.id}")
    return savedReferral.toApi()
  }

  fun calculateTotalOffenceScore(selectedOffenceIds: List<UUID>): Int = selectedOffenceIds
    .mapNotNull { offenceId ->
      sexualOffenceDetailsRepository.findById(offenceId).getOrNull()?.score
    }.sum()

  private fun createSelectedSexualOffenceDetailsEntity(referralEntity: ReferralEntity, sexualOffenceDetailsId: UUID): SelectedSexualOffenceDetailsEntity = SelectedSexualOffenceDetailsEntity(
    referral = referralEntity,
    sexualOffenceDetails = sexualOffenceDetailsRepository.findById(sexualOffenceDetailsId)
      .getOrElse { throw IllegalArgumentException("Unknown sexual offence UUID provided") }
      .also { log.warn("Could not retrieve SexualOffenceDetailsEntity with ID: $sexualOffenceDetailsId") },
  )

  fun createReferral(
    prisonNumber: String,
    offeringId: UUID,
    originalReferralId: UUID? = null,
  ): ReferralEntity {
    log.info("STARTING - Request received to create a referral for prisonNumber $prisonNumber")

    val referrerUser = getAuthenticatedReferrerUser()

    val offering = offeringRepository.findById(offeringId)
      .orElseThrow { Exception("Offering not found for $offeringId") }

    personService.createOrUpdatePerson(prisonNumber)

    organisationService.createOrganisationIfNotPresent(offering.organisationId)

    val savedReferral = referralRepository.save(
      ReferralEntity(
        offering = offering,
        prisonNumber = prisonNumber,
        referrer = referrerUser,
        originalReferralId = originalReferralId,
      ),
    ) ?: throw Exception("Referral creation failed for $prisonNumber").also { log.warn("Failed to create referral for $prisonNumber") }

    referralStatusHistoryService.createReferralHistory(savedReferral)
    auditService.audit(savedReferral, null, AuditAction.CREATE_REFERRAL.name)

    log.info("FINISHED - Request processed successfully to create a referral for prisonNumber $prisonNumber with referralId: ${savedReferral.id}")
    return savedReferral
  }

  private fun getAuthenticatedReferrerUser(): ReferrerUserEntity {
    val username = SecurityContextHolder.getContext().authentication?.name
      ?: throw SecurityException("Authentication information not found")

    val referrerUser = referrerUserRepository.findById(username).orElseGet {
      referrerUserRepository.save(ReferrerUserEntity(username = username))
    }
    return referrerUser
  }

  private fun retrieveAndStorePNI(referralId: UUID, prisonNumber: String) {
    try {
      val pniScore = pniService.getOasysPniScore(prisonNumber)
      pniService.savePni(pniScore, referralId)
    } catch (ex: Exception) {
      log.warn("PNI could not be stored ${ex.message} for prisonNumber $prisonNumber")
    }
  }

  fun getReferralById(referralId: UUID) = referralRepository.findById(referralId).getOrNull()

  fun updateReferralById(referralId: UUID, update: ReferralUpdate) {
    val referral = referralRepository.getReferenceById(referralId)
    referral.additionalInformation = update.additionalInformation ?: referral.additionalInformation
    referral.oasysConfirmed = update.oasysConfirmed
    referral.hasReviewedProgrammeHistory = update.hasReviewedProgrammeHistory
    referral.referrerOverrideReason = update.referrerOverrideReason ?: referral.referrerOverrideReason
    referral.hasLdc = update.hasLdc ?: referral.hasLdc
    referral.hasLdcBeenOverriddenByProgrammeTeam = update.hasLdcBeenOverriddenByProgrammeTeam ?: false
    referral.hasReviewedAdditionalInformation = update.hasReviewedAdditionalInformation ?: referral.hasReviewedAdditionalInformation
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

    // write case notes - buildinGChoicesCourseName is null because this is NOT a transfer from a non BC course to a BC course
    caseNotesApiService.buildAndCreateCaseNote(referral, referralStatusUpdate)

    if (referral.status == ReferralStatus.PROGRAMME_COMPLETE.name || referral.status == ReferralStatus.DESELECTED.name) {
      courseParticipationService.createOrUpdateCourseParticipation(referral)
    }

    // save PNI when referral is updated to "On Programme" status
    if (referral.status == ReferralStatus.ON_PROGRAMME.name) {
      retrieveAndStorePNI(referralId, referral.prisonNumber)
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
    if (existingStatus == ReferralStatus.ON_PROGRAMME.name && !status.first.closed) {
      val newStatus = referralStatusRepository.getByCode(ReferralStatus.DESELECTED.name)
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
      ReferralStatus.REFERRAL_STARTED.name -> {
        referral.status = ReferralStatus.REFERRAL_SUBMITTED.name
        referral.submittedOn = LocalDateTime.now()
        referral.hasLdc = pniService.hasLDC(referral.prisonNumber)
        fetchAndSavePomDetails(referral).let {
          referral.primaryPomStaffId = it?.first
          referral.secondaryPomStaffId = it?.second
        }
        caseNotesApiService.buildAndCreateCaseNote(
          referral,
          ReferralStatusUpdate(status = ReferralStatus.REFERRAL_SUBMITTED.name),
        )
        courseParticipationService.updateDraftHistoryForSubmittedReferral(referralId)
      }

      ReferralStatus.REFERRAL_SUBMITTED.name -> {
        throw IllegalArgumentException("Referral $referralId is already submitted")
      }

      ReferralStatus.AWAITING_ASSESSMENT.name -> {
        throw IllegalArgumentException("Referral $referralId is already submitted and awaiting assessment")
      }

      ReferralStatus.ASSESSMENT_STARTED.name -> {
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
    hasLdc: Boolean?,
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
        hasLdc,
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
    hasLdc: Boolean?,
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
        hasLdc,
      )

    return PageImpl(referralViewPage.content, pageable, referralViewPage.totalElements)
  }

  fun getHspReferralsView(
    pageable: Pageable,
    status: List<String>?,
    statusGroup: String?,
    prisonNumber: String?,
    surnameOnly: String?,
    forename: String?,
    surname: String?,
    hasLdc: Boolean?,
  ): Page<ReferralViewEntity> {
    val uppercaseStatuses = getFilterStatuses(status, statusGroup)
    val referralViewPage =
      referralViewRepository.getHspReferrals(
        prisonNumber = prisonNumber,
        surnameOnly = surnameOnly,
        forename = forename,
        surname = surname,
        pageable = pageable,
        status = uppercaseStatuses,
        hasLdc = hasLdc,
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
      referral.deleted = true
      referralRepository.save(referral)
    }
  }

  fun getDuplicateReferrals(prisonNumber: String, offeringId: UUID): List<ReferralEntity>? {
    val openReferralStatuses =
      referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder().map { it.code }

    return referralRepository.getReferralEntitiesByOfferingIdAndPrisonNumberAndStatusIn(
      offeringId,
      prisonNumber,
      openReferralStatuses,
    )?.filterNot { it.status == ReferralStatus.REFERRAL_STARTED.name }
  }

  fun fetchAndSavePomDetails(submittedReferral: ReferralEntity): Pair<BigInteger?, BigInteger?> = try {
    val (primaryPom, secondaryPom) = staffService.getOffenderAllocation(submittedReferral.prisonNumber)
    Pair(primaryPom?.staffId, secondaryPom?.staffId)
  } catch (ex: Exception) {
    log.error("Error fetching POM details for prison number ${submittedReferral.prisonNumber}: ${ex.message}", ex)
    Pair(null, null)
  }

  fun getPrisonIdsWithNoPrimaryPom() = referralRepository.findAllDistinctPrisonNumbersWithoutPrimaryPom()
  fun getPrisonIdsWithoutLdc() = referralRepository.findAllDistinctPrisonNumbersWithoutLdc()

  fun updatePoms(prisonNumber: String, primaryPom: StaffEntity?, secondaryPom: StaffEntity?) {
    val referrals = referralRepository.findAllByPrisonNumber(prisonNumber)
    val updatedReferrals = mutableListOf<ReferralEntity>()
    log.info("Fetched ${referrals.size} referrals for prisoner $prisonNumber referralIds ${referrals.map { it.id }}. Start updating referrals with primary pom ${primaryPom?.staffId} and secondary POM ${secondaryPom?.staffId} ")
    referrals.forEach { referral ->
      referral.primaryPomStaffId = primaryPom?.staffId
      referral.secondaryPomStaffId = secondaryPom?.staffId
      updatedReferrals.add(referral)
      log.info("Referral ${referral.id}  for prisoner $prisonNumber marked for update")
    }

    val savedReferrals = referralRepository.saveAll(updatedReferrals)
    log.info("Update successful for ${referrals.size} referrals for prisoner $prisonNumber referralIds ${savedReferrals.map { it.id }} Finished updating referrals with primary pom ${primaryPom?.staffId} and secondary POM ${secondaryPom?.staffId} ")
  }

  fun transferReferralToBuildingChoices(transferReferralRequest: TransferReferralRequest): ReferralEntity? {
    val referral = getReferralById(transferReferralRequest.referralId) ?: throw NotFoundException("No referral found with id ${transferReferralRequest.referralId}")

    val newOffering = offeringRepository.findById(transferReferralRequest.offeringId).getOrElse { throw NotFoundException("Referral ${transferReferralRequest.referralId} cannot be transferred, as offeringId ${transferReferralRequest.offeringId} does not exist") }
    val newReferral = createNewReferral(
      referral = referral,
      newOffering = newOffering,
    )
    referralStatusHistoryService.createReferralHistory(newReferral)
    auditService.audit(newReferral, null, AuditAction.CREATE_REFERRAL.name)

    updateOriginalReferralStatusToBuildingChoices(referral, transferReferralRequest)

    // buildingChoicesCourseName is passed in as the org referral is not aware of what the new bc course is
    caseNotesApiService.buildAndCreateCaseNote(
      referral = referral,
      referralStatusUpdate = ReferralStatusUpdate(
        status = ReferralStatus.MOVED_TO_BUILDING_CHOICES.name,
        notes = transferReferralRequest.transferReason,
      ),
      buildingChoicesCourseName = newOffering.course.name,
    )

    return newReferral
  }

  fun fetchCompleteReferralDataSetForId(referralId: UUID): Referral {
    log.info("Entered fetchCompleteReferralDataSetForId with referralId: $referralId")
    val referralEntity = getReferralById(referralId) ?: throw NotFoundException("No referral found with id $referralId")

    return referralEntity.run {
      auditService.audit(referralEntity = this, auditAction = AuditAction.VIEW_REFERRAL.name)
      val status = referenceDataService.getReferralStatus(this.status)
      val staffDetail = staffService.getStaffDetail(this.primaryPomStaffId)?.toApi()

      if (!this.hasLdcBeenOverriddenByProgrammeTeam) {
        this.hasLdc = getLdc(this.prisonNumber)
      }
      toApi(status, staffDetail)
    }
  }

  fun getLdc(prisonNumber: String): Boolean? = pniService.hasLDC(prisonNumber)

  private fun createNewReferral(referral: ReferralEntity, newOffering: OfferingEntity): ReferralEntity {
    val pomDetails = fetchAndSavePomDetails(referral)
    val newReferral = referralRepository.save(
      ReferralEntity(
        offering = newOffering,
        prisonNumber = referral.prisonNumber,
        referrer = getAuthenticatedReferrerUser(),
        originalReferralId = referral.id,
        status = ReferralStatus.REFERRAL_SUBMITTED.name,
        submittedOn = LocalDateTime.now(),
        primaryPomStaffId = pomDetails.first,
        secondaryPomStaffId = pomDetails.second,
      ),
    ) ?: throw IllegalStateException("New referral creation failed during transfer to building choices for ${referral.prisonNumber}").also {
      log.warn("Failed to create new referral during transfer to building choices for ${referral.prisonNumber}")
    }
    return newReferral
  }

  private fun updateOriginalReferralStatusToBuildingChoices(referral: ReferralEntity, transferReferralRequest: TransferReferralRequest) {
    val previousStatus = referral.status
    val newStatus = ReferralStatus.MOVED_TO_BUILDING_CHOICES.name

    referralStatusHistoryService.updateReferralHistory(
      referralId = referral.id!!,
      previousStatusCode = previousStatus,
      newStatus = referralStatusRepository.getByCode(newStatus),
      newNotes = transferReferralRequest.transferReason,
    )
    referral.status = newStatus
    referralRepository.save(referral)
  }

  private fun getOpenReferralStatuses() = referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder().map { it.code }
  fun getOpenReferralsForPerson(prisonNumber: String): List<ReferralEntity> = referralRepository.findAllByPrisonNumberAndStatusIn(prisonNumber, getOpenReferralStatuses())
  fun updateLdc(prisonNumber: String) {
    val hasLDC = pniService.hasLDC(prisonNumber)
    val referrals = referralRepository.findAllByPrisonNumber(prisonNumber)
    val updatedReferrals = mutableListOf<ReferralEntity>()

    referrals.forEach { referral ->
      referral.hasLdc = hasLDC
      updatedReferrals.add(referral)
      log.info("Referral ${referral.id} for prisoner $prisonNumber marked for ldc update $hasLDC")
    }

    val savedReferrals = referralRepository.saveAll(updatedReferrals)
    log.info("Update successful for ${referrals.size} referrals for prisoner $prisonNumber referralIds ${savedReferrals.map { it.id }} Finished updating referrals with ldc $hasLDC ")
  }

  fun deleteReferralsForAcpTestUser() {
    val e2eTestUsername = "ACP_TEST"
    log.info("START: Deleting all referrals for user $e2eTestUsername in $environment")
    try {
      if (environment == "dev" || environment == "local" || environment == "test") {
        val referralIds = referralViewRepository.findAllByReferralsByUsername(e2eTestUsername).map { it.id!! }
        referralStatusHistoryService.deleteReferralHistory(referralIds)
        pniService.deletePniData(referralIds)
        courseParticipationService.deleteAllCourseParticipationsForReferralIds(referralIds)
        referralRepository.deleteAllById(referralIds)

        log.info("Deleted ${referralIds.size} referrals for user $e2eTestUsername with ids $referralIds")
      } else {
        throw IllegalStateException("Delete referrals for user $e2eTestUsername is not allowed in $environment environment")
      }
      log.info("FINISH: Deleting referrals for user $e2eTestUsername in $environment successful")
    } catch (e: Exception) {
      log.error("Error deleting referrals for user $e2eTestUsername", e)
      throw e
    }
  }
}
