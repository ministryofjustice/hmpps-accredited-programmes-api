package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

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

  private companion object {
    private val tsp = CourseEntity(
      id = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      name = "Lime Course",
      type = "Accredited Programme",
      description = "Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.",
      prerequisites = listOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = emptyList(),
    )

    private val bnm = CourseEntity(
      id = UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      name = "Azure Course",
      type = "Accredited Programme",
      description = "Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.",
      prerequisites = listOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = listOf(Audience(value = "Sexual violence")),
    )

    private val nms = CourseEntity(
      id = UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      name = "Violet Course",
      type = "Accredited Programme",
      description = "Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.",
      prerequisites = listOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = emptyList(),
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
