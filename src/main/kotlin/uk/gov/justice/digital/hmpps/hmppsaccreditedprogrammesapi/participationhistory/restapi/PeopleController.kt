package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PeopleApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService

@Service
class PeopleController(
  @Autowired val service: CourseParticipationHistoryService,
) : PeopleApiDelegate {
  override fun getCourseParticipationsForPrisonNumber(prisonNumber: String): ResponseEntity<List<CourseParticipation>> =
    ResponseEntity.ok(
      service
        .findByPrisonNumber(prisonNumber)
        .map(CourseParticipationHistory::toApi),
    )
}
