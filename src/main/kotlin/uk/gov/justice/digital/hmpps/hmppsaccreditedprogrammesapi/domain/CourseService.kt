package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.time.Duration

@Service
class CourseService(
  @Autowired
  courseRepository: CourseRepository,
) {
  fun allCourses(): List<CourseEntity> = courses.toList()

  fun course(courseId: UUID): CourseEntity? =
    courses
      .find { it.id == courseId }

  fun offeringsForCourse(courseId: UUID): List<Offering> =
    offerings
      .filter { it.course.id == courseId }
      .toList()

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? =
    offerings
      .find { it.id == offeringId && it.course.id == courseId }

  companion object {
    private val tsp = CourseEntity(
      name = "Thinking Skills Programme",
      type = "TypeA",
      description = "Thinking Skills Programme (TSP) is for adult men and women with a medium or high risk of re offending...",
      prerequisites = listOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
      audience = listOf(Audience("Sexual offence")),
    )

    private val bnm = CourseEntity(
      name = "Becoming new me +",
      type = "TypeB",
      description = "Becoming new me + is for adult men and women with a medium or high risk of re offending...",
      prerequisites = listOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
      audience = listOf(Audience("Extremism"), Audience("General violence")),
    )

    private val nms = CourseEntity(

      name = "New me strengths",
      type = "TypeA",
      description = "New me strengths is for adult men and women with a medium or high risk of re offending...",
      prerequisites = listOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
      audience = listOf(Audience("General violence")),
    )

    private val courses: Set<CourseEntity> = setOf(tsp, bnm, nms)

    private val offerings: Set<Offering> = setOf(
      Offering(
        organisationId = "MDI",
        duration = Duration.parse("P10D"),
        groupSize = 10,
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        organisationId = "BWN",
        duration = Duration.parse("P8D"),
        groupSize = 6,
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        organisationId = "BXI",
        duration = Duration.parse("P8D"),
        groupSize = 6,
        contactEmail = "nobody-bxi@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        organisationId = "MDI",
        duration = Duration.parse("P4D"),
        groupSize = 2,
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = bnm,
      ),

      Offering(
        organisationId = "BWN",
        duration = Duration.parse("P3D"),
        groupSize = 12,
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = nms,
      ),
    )
  }
}
