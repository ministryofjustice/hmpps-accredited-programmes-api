package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.inmemoryrepo

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.UUID

@Component
class InMemoryCourseRepository : CourseRepository {

  override fun allCourses(): List<CourseEntity> = courses.toList()

  override fun course(courseId: UUID): CourseEntity? =
    courses
      .find { it.id == courseId }

  override fun offeringsForCourse(courseId: UUID): List<Offering> =
    offerings
      .filter { it.course.id == courseId }
      .toList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? =
    offerings
      .find { it.id == offeringId && it.course.id == courseId }

  private companion object {
    private val tsp = CourseEntity(
      id = UUID.randomUUID(),
      name = "Thinking Skills Programme",
      type = "TypeA",
      description = "Thinking Skills Programme (TSP) is for adult men and women with a medium or high risk of re offending...",
      prerequisites = mutableSetOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
    )

    private val bnm = CourseEntity(
      id = UUID.randomUUID(),
      name = "Becoming new me +",
      type = "TypeB",
      description = "Becoming new me + is for adult men and women with a medium or high risk of re offending...",
      prerequisites = mutableSetOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
    )

    private val nms = CourseEntity(
      id = UUID.randomUUID(),
      name = "New me strengths",
      type = "TypeA",
      description = "New me strengths is for adult men and women with a medium or high risk of re offending...",
      prerequisites = mutableSetOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
        Prerequisite(name = "offence type", description = "some offence here"),
      ),
    )

    private val courses: Set<CourseEntity> = setOf(tsp, bnm, nms)

    private val offerings: Set<Offering> = setOf(
      Offering(
        id = UUID.randomUUID(),
        organisationId = "MDI",
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        id = UUID.randomUUID(),
        organisationId = "BWN",
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        id = UUID.randomUUID(),
        organisationId = "BXI",
        contactEmail = "nobody-bxi@digital.justice.gov.uk",
        course = tsp,
      ),

      Offering(
        id = UUID.randomUUID(),
        organisationId = "MDI",
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = bnm,
      ),

      Offering(
        id = UUID.randomUUID(),
        organisationId = "BWN",
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = nms,
      ),
    )
  }
}
