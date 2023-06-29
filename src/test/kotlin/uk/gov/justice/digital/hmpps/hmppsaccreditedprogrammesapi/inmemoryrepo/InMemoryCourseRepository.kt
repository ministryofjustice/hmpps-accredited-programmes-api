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

  override fun course(courseId: UUID): CourseEntity? = courses.find { it.id == courseId }

  override fun offeringsForCourse(courseId: UUID): List<Offering> =
    courses.find { it.id == courseId }?.offerings?.toList() ?: emptyList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? =
    courses.find { it.id == courseId }?.offerings?.find { it.id == offeringId }

  override fun allAudiences(): Set<Audience> = audiences

  private companion object {
    private val audiences = setOf(Audience(value = "Sexual violence", id = UUID.randomUUID()))

    private val tsp = CourseEntity(
      id = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      name = "Lime Course",
      identifier = "LC-VO",
      description = "Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      alternateName = "LC",
      audiences = mutableSetOf(),
    ).apply {
      offerings.add(Offering(organisationId = "MDI", contactEmail = "nobody-mdi@digital.justice.gov.uk"))
      offerings.add(Offering(organisationId = "BWN", contactEmail = "nobody-bwn@digital.justice.gov.uk"))
      offerings.add(Offering(organisationId = "BXI", contactEmail = "nobody-bxi@digital.justice.gov.uk"))
    }

    private val bnm = CourseEntity(
      id = UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      name = "Azure Course",
      identifier = "AC-SO",
      description = "Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      alternateName = "AC++",
      audiences = audiences.toMutableSet(),
    ).apply { offerings.add(Offering(organisationId = "MDI", contactEmail = "nobody-mdi@digital.justice.gov.uk")) }

    private val nms = CourseEntity(
      id = UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      name = "Violet Course",
      identifier = "VC-VO",
      description = "Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.",
      prerequisites = mutableSetOf(
        Prerequisite(name = "Setting", description = "Custody"),
        Prerequisite(name = "Risk criteria", description = "High ESARA/SARA/OVP, High OGRS"),
        Prerequisite(name = "Criminogenic needs", description = "Relationships, Thinking and Behaviour, Attitudes, Lifestyle"),
      ),
      audiences = mutableSetOf(),
    ).apply { offerings.add(Offering(organisationId = "BWN", contactEmail = "nobody-bwn@digital.justice.gov.uk")) }

    private val courses: Set<CourseEntity> = setOf(tsp, bnm, nms)
  }
}
