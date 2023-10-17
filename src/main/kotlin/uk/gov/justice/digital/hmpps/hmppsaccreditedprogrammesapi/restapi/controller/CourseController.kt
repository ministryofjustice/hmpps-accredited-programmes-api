package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toCourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import java.util.UUID

@Service
class CourseController
@Autowired
constructor(
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

  override fun updateCourses(courseRecord: List<CourseRecord>): ResponseEntity<Unit> {
    courseService.updateCourses(courseRecord.map(CourseRecord::toDomain))
    return ResponseEntity.noContent().build()
  }

  override fun updatePrerequisites(prerequisiteRecord: List<PrerequisiteRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.updatePrerequisites(prerequisiteRecord.map(PrerequisiteRecord::toDomain)))

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
          .map(OfferingEntity::toApi),
      )
}
