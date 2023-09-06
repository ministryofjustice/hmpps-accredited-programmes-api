package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CourseParticipationHistoryApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import java.util.UUID

@Service
class CourseParticipationHistoryController(
  @Autowired val service: CourseParticipationHistoryService,
) : CourseParticipationHistoryApiDelegate {
  override fun courseParticipationHistoryPost(createCourseParticipation: CreateCourseParticipation): ResponseEntity<CourseParticipation> =
    service.addCourseParticipation(createCourseParticipation.toDomain())
      ?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(it.toApi())
      } ?: throw Exception("Unable to add to course participation history")

  override fun courseParticipationHistoryHistoricCourseParticipationIdGet(historicCourseParticipationId: UUID): ResponseEntity<CourseParticipation> =
    service.getCourseParticipationHistory(historicCourseParticipationId)
      ?.let {
        ResponseEntity.ok(it.toApi())
      } ?: throw NotFoundException("No course participation history found for id $historicCourseParticipationId")

  override fun courseParticipationHistoryHistoricCourseParticipationIdPut(historicCourseParticipationId: UUID, courseParticipationUpdate: CourseParticipationUpdate): ResponseEntity<CourseParticipation> =
    ResponseEntity.ok(service.updateCourseParticipationHistory(historicCourseParticipationId, courseParticipationUpdate.toDomain()).toApi())

  override fun courseParticipationHistoryGet(prisonNumber: String): ResponseEntity<List<CourseParticipation>> =
    ResponseEntity.ok(
      service
        .findByPrisonNumber(prisonNumber)
        .map(CourseParticipationHistory::toApi),
    )

  override fun courseParticipationHistoryHistoricCourseParticipationIdDelete(historicCourseParticipationId: UUID): ResponseEntity<Unit> {
    service.deleteCourseParticipation(historicCourseParticipationId)
    return ResponseEntity.noContent().build()
  }
}
