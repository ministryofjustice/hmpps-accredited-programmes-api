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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationService
import java.util.UUID

@Service
class CourseParticipationController
@Autowired
constructor (
  private val courseParticipationService: CourseParticipationService,
) : CourseParticipationsApiDelegate {
  override fun createCourseParticipation(createCourseParticipation: CreateCourseParticipation): ResponseEntity<CourseParticipation> =
    courseParticipationService.createCourseParticipation(createCourseParticipation.toDomain())
      ?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(it.toApi())
      } ?: throw Exception("Unable to add to course participation")
  override fun getCourseParticipationById(id: UUID): ResponseEntity<CourseParticipation> =
    courseParticipationService.getCourseParticipationById(id)
      ?.let {
        ResponseEntity.ok(it.toApi())
      } ?: throw NotFoundException("No course participation found for id $id")

  override fun updateCourseParticipationById(id: UUID, courseParticipationUpdate: CourseParticipationUpdate): ResponseEntity<CourseParticipation> =
    ResponseEntity.ok(courseParticipationService.updateCourseParticipationById(id, courseParticipationUpdate.toDomain()).toApi())

  override fun deleteCourseParticipationById(id: UUID): ResponseEntity<Unit> {
    courseParticipationService.deleteCourseParticipationById(id)
    return ResponseEntity.noContent().build()
  }
}
