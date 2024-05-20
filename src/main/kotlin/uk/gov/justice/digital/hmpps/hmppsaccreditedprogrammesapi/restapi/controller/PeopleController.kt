package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PeopleApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ManageOffencesService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as CourseParticipationApi

@Service
class PeopleController
@Autowired
constructor(
  private val courseParticipationService: CourseParticipationService,
  private val personService: PersonService,
  private val manageOffencesService: ManageOffencesService,
) : PeopleApiDelegate {
  override fun getCourseParticipationsByPrisonNumber(prisonNumber: String): ResponseEntity<List<CourseParticipationApi>> =
    ResponseEntity.ok(
      courseParticipationService
        .getCourseParticipationsByPrisonNumber(prisonNumber)
        .map(CourseParticipationEntity::toApi),
    )

  override fun getSentenceDetails(prisonNumber: String): ResponseEntity<SentenceDetails> =
    ResponseEntity.ok(
      personService
        .getSentenceDetails(prisonNumber),
    )

  override fun getOffences(prisonNumber: String): ResponseEntity<List<Offence>> {
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
}
