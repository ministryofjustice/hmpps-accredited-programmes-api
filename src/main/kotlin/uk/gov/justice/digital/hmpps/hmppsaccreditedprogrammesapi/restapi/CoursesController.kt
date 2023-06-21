package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer.toApi
import java.util.UUID

@Service
class CoursesController(
  val courseService: CourseService,
) : CoursesApiDelegate {
  override fun coursesGet(): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .allCourses()
          .map(CourseEntity::toApi),
      )

  override fun coursesPut(courseRecords: List<CourseRecord>): ResponseEntity<Unit> {
    courseService.replaceAllCourses(courseRecords)
    return ResponseEntity.noContent().build()
  }

  override fun coursesCourseIdGet(courseId: UUID): ResponseEntity<Course> =
    courseService.course(courseId)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /courses/$courseId")

  override fun coursesCourseIdOfferingsGet(courseId: UUID): ResponseEntity<List<CourseOffering>> =
    ResponseEntity
      .ok(
        courseService
          .offeringsForCourse(courseId)
          .map(Offering::toApi),
      )

  override fun coursesCourseIdOfferingsOfferingIdGet(courseId: UUID, offeringId: UUID): ResponseEntity<CourseOffering> =
    courseService.courseOffering(courseId, offeringId)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No CourseOffering  found at /courses/$courseId/offerings/$offeringId")
}
