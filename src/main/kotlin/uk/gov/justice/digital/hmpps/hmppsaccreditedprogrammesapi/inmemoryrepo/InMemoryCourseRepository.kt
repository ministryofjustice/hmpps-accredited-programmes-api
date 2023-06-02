package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.inmemoryrepo

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.UUID

@Component
@Qualifier("InMemory")
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
    private val limeCourse = CourseEntity(
      id = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      name = "Lime Course",
      type = "Accredited Programme",
      description = "Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = mutableSetOf(),
    )

    private val azureCourse = CourseEntity(
      id = UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      name = "Azure Course",
      type = "Accredited Programme",
      description = "Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = mutableSetOf(Audience(id = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), value = "Sexual violence")),
    )

    private val violetCourse = CourseEntity(
      id = UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      name = "Violet Course",
      type = "Accredited Programme",
      description = "Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = mutableSetOf(),
    )

    private val courses: Set<CourseEntity> = setOf(limeCourse, azureCourse, violetCourse)

    private val offerings: Set<Offering> = setOf(
      Offering(
        id = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
        organisationId = "MDI",
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = limeCourse,
      ),

      Offering(
        id = UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
        organisationId = "BWN",
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = limeCourse,
      ),

      Offering(
        id = UUID.fromString("39b77a2f-7398-4d5f-b744-cdcefca12671"),
        organisationId = "BXI",
        contactEmail = "nobody-bxi@digital.justice.gov.uk",
        course = limeCourse,
      ),

      Offering(
        id = UUID.fromString("ee20f564-4853-40b2-bae4-65dd7e0207fa"),
        organisationId = "MDI",
        contactEmail = "nobody-mdi@digital.justice.gov.uk",
        course = azureCourse,
      ),

      Offering(
        id = UUID.fromString("b328ebc8-1f7b-4236-b4ac-30f50b43a92d"),
        organisationId = "BWN",
        contactEmail = "nobody-bwn@digital.justice.gov.uk",
        course = violetCourse,
      ),
    )
  }
}
