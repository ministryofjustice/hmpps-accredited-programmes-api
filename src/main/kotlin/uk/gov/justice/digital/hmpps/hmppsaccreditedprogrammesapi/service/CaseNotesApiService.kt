package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.CaseNotesApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model.CaseNoteCreatedResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model.CaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
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
  private val organisationRepository: OrganisationRepository,
  private val referralStatusRepository: ReferralStatusRepository,
  private val referralStatusReasonRepository: ReferralStatusReasonRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private fun createCaseNote(caseNoteRequest: CaseNoteRequest, prisonNumber: String): CaseNoteCreatedResponse? {
    val createdCaseNote = when (val response = caseNotesApiClient.createCaseNote(caseNoteRequest, prisonNumber)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

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
        val person = personService.getPerson(referral.prisonNumber)!!
        val referralStatusEntity = referralStatusRepository.getByCode(referralStatusUpdate.status)
        val message = buildMessage(person, referral, referralStatusUpdate, referralStatusEntity.caseNotesMessage)
        log.warn("************* caseNoteMessage: $message")
        val createdCaseNote = createCaseNote(
          CaseNoteRequest(
            type = ACP_TYPE,
            subType = referralStatusEntity.caseNotesSubtype,
            occurrenceDateTime = LocalDateTime.now().toString(),
            authorName = ACP_USER,
            text = message,
          ),
          referral.prisonNumber,
        )

        log.info("Automatic case note with id ${createdCaseNote?.caseNoteId} created for ${referral.prisonNumber} with message $message")
      }
    } catch (ex: Exception) {
      log.warn("Error writing case notes for ${referral.prisonNumber} ${ex.message} $ex")
    }
  }

  fun buildMessage(
    person: PersonEntity?,
    referral: ReferralEntity,
    referralStatusUpdate: ReferralStatusUpdate,
    message: String,
  ): String {
    log.info("Request received for creating case notes :${referral.id} $referralStatusUpdate")
    val course = referral.offering.course
    val orgName = organisationRepository.findOrganisationEntityByCode(referral.offering.organisationId)?.name
    val progDescMessage = "Referral to ${course.name}: ${course.audience} strand at $orgName \n"

    val prisonerName = person?.fullName().orEmpty()
    val pgmNameAndStrand = "${course.name} : ${course.audience}"
    val customMessage = message.replace("PRISONER_NAME", prisonerName).replace("PGM_NAME_STRAND", pgmNameAndStrand) + "\n"
    val details = referralStatusUpdate.notes?.trim()?.let { "Details: ${referralStatusUpdate.notes} \n" }.orEmpty()

    val reasonForClosingReferral =
      if (referral.status == "WITHDRAWN" || referral.status == "DESELECTED") {
        val referralStatusReason = referralStatusReasonRepository.findByCode(referralStatusUpdate.reason!!)
        "Reason for closing referral: ${referralStatusReason?.description} \n"
      } else {
        ""
      }

    return progDescMessage + customMessage + reasonForClosingReferral + details
  }
}
