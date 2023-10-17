package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PeopleApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as CourseParticipationApi

@Service
class PeopleController
@Autowired
constructor (
  private val courseParticipationService: CourseParticipationService,
) : PeopleApiDelegate {
  override fun getCourseParticipationsByPrisonNumber(prisonNumber: String): ResponseEntity<List<CourseParticipationApi>> =
    ResponseEntity.ok(
      courseParticipationService
        .getCourseParticipationsByPrisonNumber(prisonNumber)
        .map(CourseParticipation::toApi),
    )
}
