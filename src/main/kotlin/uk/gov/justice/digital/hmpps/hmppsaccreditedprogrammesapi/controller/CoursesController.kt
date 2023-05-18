package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import java.util.UUID

@Service
class CoursesController : CoursesApiDelegate {
  override fun coursesGet(): ResponseEntity<List<Course>> {
    return ResponseEntity.ok(
      listOf(
        Course(
          id = UUID.randomUUID(),
          name = "Thinking Skills Programme",
          type = "TypeA",
          description = "Thinking Skills Programme (TSP) is for adult men and women with a medium or high risk of re offending...",
          coursePrerequisites = listOf(
            CoursePrerequisite(name = "gender", description = "female"),
            CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
            CoursePrerequisite(name = "offence type", description = "some offence here"),
          ),
        ),
        Course(
          id = UUID.randomUUID(),
          name = "Becoming new me +",
          type = "TypeB",
          description = "Becoming new me + is for adult men and women with a medium or high risk of re offending...",
          coursePrerequisites = listOf(
            CoursePrerequisite(name = "gender", description = "female"),
            CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
            CoursePrerequisite(name = "offence type", description = "some offence here"),
          ),
        ),
        Course(
          id = UUID.randomUUID(),
          name = "New me strengths",
          type = "TypeA",
          description = "New me strengths is for adult men and women with a medium or high risk of re offending...",
          coursePrerequisites = listOf(
            CoursePrerequisite(name = "gender", description = "female"),
            CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
            CoursePrerequisite(name = "offence type", description = "some offence here"),
          ),
        ),
      ),
    )
  }
}
