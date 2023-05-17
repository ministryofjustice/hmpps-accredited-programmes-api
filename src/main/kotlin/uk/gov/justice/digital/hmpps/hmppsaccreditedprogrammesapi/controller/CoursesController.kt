package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer.toApi

@Service
class CoursesController(
  val courseService: CourseService,
) : CoursesApiDelegate {
  override fun coursesGet(): ResponseEntity<List<Course>> = ResponseEntity.ok(courseService.allCourses().map(CourseEntity::toApi))
}
