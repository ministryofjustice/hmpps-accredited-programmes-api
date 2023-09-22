package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CourseParticipationsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import java.util.UUID

@Service
class CourseParticipationHistoryController(
  @Autowired val service: CourseParticipationHistoryService,
) : CourseParticipationsApiDelegate {
  override fun createCourseParticipation(createCourseParticipation: CreateCourseParticipation): ResponseEntity<CourseParticipation> =
    service.addCourseParticipation(createCourseParticipation.toDomain())
      ?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(it.toApi())
      } ?: throw Exception("Unable to add to course participation history")

  override fun getCourseParticipation(courseParticipationId: UUID): ResponseEntity<CourseParticipation> =
    service.getCourseParticipationHistory(courseParticipationId)
      ?.let {
        ResponseEntity.ok(it.toApi())
      } ?: throw NotFoundException("No course participation history found for id $courseParticipationId")

  override fun updateCourseParticipation(courseParticipationId: UUID, courseParticipationUpdate: CourseParticipationUpdate): ResponseEntity<CourseParticipation> =
    ResponseEntity.ok(service.updateCourseParticipationHistory(courseParticipationId, courseParticipationUpdate.toDomain()).toApi())

  override fun deleteCourseParticipation(courseParticipationId: UUID): ResponseEntity<Unit> {
    service.deleteCourseParticipation(courseParticipationId)
    return ResponseEntity.noContent().build()
  }
}
