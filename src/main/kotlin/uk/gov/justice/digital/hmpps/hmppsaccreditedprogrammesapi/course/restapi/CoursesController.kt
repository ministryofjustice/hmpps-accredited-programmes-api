package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired

@Service
class CoursesController
@Autowired
constructor (
  private val courseService: CourseService,
) : CoursesApiDelegate {
  override fun getAllCourses(): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllCourses()
          .map(CourseEntity::toApi),
      )

  override fun getCoursesCsv(): ResponseEntity<List<CourseRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllCourses()
        .map(CourseEntity::toCourseRecord),
    )

  override fun uploadCoursesCsv(courseRecord: List<CourseRecord>): ResponseEntity<Unit> {
    courseService.uploadCoursesCsv(courseRecord.map(CourseRecord::toDomain))
    return ResponseEntity.noContent().build()
  }

  override fun uploadPrerequisitesCsv(prerequisiteRecord: List<PrerequisiteRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.uploadPrerequisitedCsv(prerequisiteRecord.map(PrerequisiteRecord::toDomain)))

  override fun getPrerequisitesCsv(): ResponseEntity<List<PrerequisiteRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllCourses()
        .flatMap { course ->
          course.prerequisites.map { prerequisite ->
            PrerequisiteRecord(
              name = prerequisite.name,
              description = prerequisite.description,
              course = course.name,
              identifier = course.identifier,
            )
          }
        },
    )

  override fun getCourseById(id: UUID): ResponseEntity<Course> =
    courseService.getCourseById(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /courses/$id")

  override fun getAllOfferingsByCourseId(id: UUID): ResponseEntity<List<CourseOffering>> =
    ResponseEntity
      .ok(
        courseService
          .getAllOfferingsByCourseId(id)
          .map(Offering::toApi),
      )
}
