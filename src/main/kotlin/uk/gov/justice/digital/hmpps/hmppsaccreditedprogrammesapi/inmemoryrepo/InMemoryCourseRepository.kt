package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.inmemoryrepo

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
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
      id = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      name = "Lime Course",
      type = "Accredited Programme",
      description = "Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = setOf(),
    )

    private val bnm = CourseEntity(
      id = UUID.fromString("1e8eb213-92ce-402a-bb1a-65fba86c361c"),
      name = "Azure Course",
      type = "Accredited Programme",
      description = "Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = setOf(Audience(id = UUID.randomUUID(), value = "Sexual violence")),
    )

    private val nms = CourseEntity(
      id = UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      name = "Violet Course",
      type = "Accredited Programme",
      description = "Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = setOf(),
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
