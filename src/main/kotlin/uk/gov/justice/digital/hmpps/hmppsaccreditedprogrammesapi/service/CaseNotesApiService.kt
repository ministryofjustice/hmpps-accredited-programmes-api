package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesApi.CaseNotesApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesApi.model.CaseNoteCreatedResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesApi.model.CaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import java.time.LocalDateTime

private const val ACP_USER = "Accredited Programmes automated case note"

private const val ACP_TYPE = "ACP"

@Service
@Transactional
class CaseNotesApiService(
  private val caseNotesApiClient: CaseNotesApiClient,
  private val featureSwitchService: FeatureSwitchService,
  private val personService: PersonService,
  private val organisationService: OrganisationService,
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
  private val manageUsersService: ManageUsersService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private fun createCaseNote(caseNoteRequest: CaseNoteRequest, prisonNumber: String): CaseNoteCreatedResponse? {
    val createdCaseNote = when (val response = caseNotesApiClient.createCaseNote(caseNoteRequest, prisonNumber)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> {
        log.warn("Failure to create case note for prisonNumbers $prisonNumber Reason ${response.toException().message} ")
        throw ServiceUnavailableException(
          "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
          response.toException(),
        )
      }

      is ClientResult.Failure -> {
        log.warn("Failure to create case note for prisonNumbers $prisonNumber Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(null)
      }
    }
    return createdCaseNote.entity
  }

  fun buildAndCreateCaseNote(referral: ReferralEntity, referralStatusUpdate: ReferralStatusUpdate) {
    try {
      if (featureSwitchService.isCaseNotesEnabled()) {
        log.info("START - Request received to create automatic case notes for ${referral.prisonNumber} $referralStatusUpdate")

        val person = personService.getPerson(referral.prisonNumber)
        val referralStatusEntity = referralStatusRepository.getByCode(referralStatusUpdate.status)

        val message =
          buildCaseNoteMessage(person, referral, referralStatusUpdate, referralStatusEntity.caseNotesMessage)

        log.info("Building case note request object for ${referral.prisonNumber}")

        val caseNoteRequest = CaseNoteRequest(
          type = ACP_TYPE,
          subType = referralStatusEntity.caseNotesSubtype,
          occurrenceDateTime = LocalDateTime.now().toString(),
          authorName = ACP_USER,
          text = message,
          locationId = getLocationId(person),
        )

        val createdCaseNote = createCaseNote(
          caseNoteRequest,
          referral.prisonNumber,
        )

        log.info("FINISH - Automatic case note with id ${createdCaseNote?.caseNoteId} created for ${referral.prisonNumber} ")
      }
    } catch (ex: Exception) {
      log.warn("Error writing case notes for ${referral.prisonNumber}", ex)
    }
  }

  private fun getLocationId(person: PersonEntity?): String {
    var location = ""
    try {
      location = if (person?.location.equals("RELEASED", ignoreCase = true)) {
        "OUT"
      } else {
        return person?.location?.run {
          organisationService.findOrganisationEntityByName(this)?.code ?: location
        } ?: location
      }
    } catch (ex: Exception) {
      log.warn("Error getting location id for ${person?.location}. Will write case-notes with location as null.", ex)
      return location
    }

    log.info("Location of person: ${person?.location}  code: $location")
    return location
  }

  fun buildCaseNoteMessage(
    person: PersonEntity?,
    referral: ReferralEntity,
    referralStatusUpdate: ReferralStatusUpdate,
    message: String,
  ): String {
    log.info("Building case notes message :${referral.id} $referralStatusUpdate")

    val course = referral.offering.course
    val orgName = organisationService.findOrganisationEntityByCode(referral.offering.organisationId)?.name

    val programmeDescriptionMessage = "Referral to ${course.name}: ${course.audience} strand at $orgName \n\n"

    log.info("programmeDescriptionMessage Course and org name : $programmeDescriptionMessage")

    val prisonerName = person?.fullName().orEmpty()
    val programNameAndStrand = "${course.name}: ${course.audience}"

    log.info("programNameAndStrand : \n $programNameAndStrand")

    val customMessage =
      message.replace("PRISONER_NAME", prisonerName).replace("PGM_NAME_STRAND", programNameAndStrand) + "\n"

    val details = referralStatusUpdate.notes
      ?.takeIf { it.isNotBlank() }
      ?.let { "Details: $it \n\n" }
      ?: ""

    val reasonForClosingReferral =
      if (referral.status == "WITHDRAWN" || referral.status == "DESELECTED") {
        val referralStatusReason = referralStatusReasonRepository.findByCode(referralStatusUpdate.reason!!)
        "Reason for closing referral: ${referralStatusReason?.description} \n\n"
      } else {
        "\n"
      }

    val statusUpdatedBy = "Updated by: ${getFullName()}\n"

    return programmeDescriptionMessage + customMessage + reasonForClosingReferral + details + statusUpdatedBy
  }

  fun getFullName(): String? {
    val username = SecurityContextHolder.getContext().authentication?.name!!
    return try {
      manageUsersService.getUserDetail(username)?.name ?: username
    } catch (ex: Exception) {
      log.warn("Error getting full name for username $username. Will write case-notes with username instead. $ex")
      username
    }
  }
}
