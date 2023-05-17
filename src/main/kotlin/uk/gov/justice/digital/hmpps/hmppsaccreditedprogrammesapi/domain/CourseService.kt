package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CourseService {
  fun allCourses(): List<CourseEntity> =
    listOf(
      CourseEntity(
        id = UUID.randomUUID(),
        name = "Thinking Skills Programme",
        type = "TypeA",
        description = "Thinking Skills Programme (TSP) is for adult men and women with a medium or high risk of re offending...",
        prerequisites = listOf(
          PrerequisiteEntity(name = "gender", description = "female"),
          PrerequisiteEntity(name = "risk score", description = "ORGS: 50+"),
          PrerequisiteEntity(name = "offence type", description = "some offence here"),
        ),
      ),
      CourseEntity(
        id = UUID.randomUUID(),
        name = "Becoming new me +",
        type = "TypeB",
        description = "Becoming new me + is for adult men and women with a medium or high risk of re offending...",
        prerequisites = listOf(
          PrerequisiteEntity(name = "gender", description = "female"),
          PrerequisiteEntity(name = "risk score", description = "ORGS: 50+"),
          PrerequisiteEntity(name = "offence type", description = "some offence here"),
        ),
      ),
      CourseEntity(
        id = UUID.randomUUID(),
        name = "New me strengths",
        type = "TypeA",
        description = "New me strengths is for adult men and women with a medium or high risk of re offending...",
        prerequisites = listOf(
          PrerequisiteEntity(name = "gender", description = "female"),
          PrerequisiteEntity(name = "risk score", description = "ORGS: 50+"),
          PrerequisiteEntity(name = "offence type", description = "some offence here"),
        ),
      ),
    )
}
