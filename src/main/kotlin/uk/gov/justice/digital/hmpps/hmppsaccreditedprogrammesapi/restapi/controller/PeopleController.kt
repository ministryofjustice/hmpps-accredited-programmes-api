package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PeopleApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ManageOffencesService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as CourseParticipationApi

@Service
class PeopleController
@Autowired
constructor(
  private val courseParticipationService: CourseParticipationService,
  private val personService: PersonService,
  private val manageOffencesService: ManageOffencesService,
  private val peopleSearchApiService: PeopleSearchApiService,
  private val auditService: AuditService,

) : PeopleApiDelegate {
  override fun getCourseParticipationsByPrisonNumber(prisonNumber: String): ResponseEntity<List<CourseParticipationApi>> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.COURSE_PARTICIPATION.name,
    )
    return ResponseEntity.ok(
      courseParticipationService
        .getCourseParticipationsByPrisonNumber(prisonNumber)
        .map(CourseParticipationEntity::toApi),
    )
  }

  override fun getSentenceDetails(prisonNumber: String): ResponseEntity<SentenceDetails> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.SENTENCE_DETAILS.name,
    )
    return ResponseEntity.ok(
      personService
        .getSentenceDetails(prisonNumber),
    )
  }

  override fun getOffences(prisonNumber: String): ResponseEntity<List<Offence>> {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.OFFENCE_DETAILS.name,
    )
    val offenceMap = personService.getOffenceDetails(prisonNumber).associateBy({ it.first }, { it.second })
    val offences = manageOffencesService.getOffences(offenceMap.keys.toList())

    return ResponseEntity.ok(
      offences.map { offence ->
        Offence(
          offence = "${offence.description} - ${offence.code}",
          category = offence.legislation,
          offenceDate = offenceMap[offence.code],
        )
      },
    )
  }

  override fun searchPeople(peopleSearchRequest: PeopleSearchRequest): ResponseEntity<List<PeopleSearchResponse>> {
    auditService.audit(
      prisonNumber = peopleSearchRequest.prisonerIdentifier,
      auditAction = AuditAction.NOMIS_SEARCH_FOR_PERSON.name,
    )

    return ResponseEntity.ok(
      peopleSearchApiService.searchPeople(peopleSearchRequest)
        .map {
          PeopleSearchResponse(
            bookingId = it.bookingId,
            conditionalReleaseDate = it.conditionalReleaseDate,
            prisonName = it.prisonName,
            dateOfBirth = it.dateOfBirth,
            ethnicity = it.ethnicity,
            gender = it.gender,
            homeDetentionCurfewEligibilityDate = it.homeDetentionCurfewEligibilityDate,
            indeterminateSentence = it.indeterminateSentence,
            firstName = it.firstName,
            lastName = it.lastName,
            paroleEligibilityDate = it.paroleEligibilityDate,
            prisonerNumber = it.prisonerNumber,
            religion = it.religion,
            sentenceExpiryDate = it.sentenceExpiryDate,
            sentenceStartDate = it.sentenceStartDate,
            tariffDate = it.tariffDate,
          )
        },
    )
  }
}
