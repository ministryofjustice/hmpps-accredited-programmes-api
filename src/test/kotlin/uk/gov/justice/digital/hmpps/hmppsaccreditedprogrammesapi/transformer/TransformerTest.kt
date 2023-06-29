package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.UUID

class TransformerTest {
  @Test
  fun `transform course entity to api missing description, alternateName and no prerequisites`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      identifier = "C-VO",
      name = "A Course",
      prerequisites = mutableSetOf(),
      audiences = mutableSetOf(),
    )

    with(entity.toApi()) {
      id shouldBe entity.id
      name shouldBe entity.name
      description shouldBe null
      alternateName shouldBe null
      coursePrerequisites.shouldBeEmpty()
    }
  }

  @Test
  fun `transform course entity to api with description and alternateName`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      identifier = "C-VO",
      name = "A Course",
      description = "A description",
      alternateName = "AA++",
      prerequisites = mutableSetOf(),
      audiences = mutableSetOf(),
    )

    with(entity.toApi()) {
      description shouldBe entity.description
      alternateName shouldBe entity.alternateName
    }
  }

  @Test
  fun `transform course entity to api with prerequisites and audience`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      identifier = "C-VO",
      prerequisites = mutableSetOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
      ),
      audiences = mutableSetOf(
        Audience(value = "A", id = UUID.randomUUID()),
        Audience(value = "B", id = UUID.randomUUID()),
        Audience(value = "C", id = UUID.randomUUID()),
      ),
    )

    with(entity.toApi()) {
      coursePrerequisites shouldContainExactlyInAnyOrder listOf(
        CoursePrerequisite(name = "gender", description = "female"),
        CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
      )
      audiences.map { it.value } shouldContainExactlyInAnyOrder listOf("C", "B", "A")
    }
  }

  @Test
  fun `transform a course prerequisite entity to api`() {
    val entity = Prerequisite(
      name = "gender",
      description = "female",
    )

    with(entity.toApi()) {
      name shouldBe entity.name
      description shouldBe description
    }
  }

  @Test
  fun `transform a domain offering to a api CourseOffering`() {
    val offering = Offering(
      organisationId = "BXI",
      contactEmail = "nobody-bwn@digital.justice.gov.uk",
    )

    with(offering.toApi()) {
      id shouldBe offering.id
      organisationId shouldBe offering.organisationId
      contactEmail shouldBe offering.contactEmail
    }
  }

  @Test
  fun `transform domain Audience to a CourseAudience`() {
    val audience = Audience(value = "An audience", id = UUID.randomUUID())
    with(audience.toApi()) {
      id shouldBe audience.id
      value shouldBe audience.value
    }
  }
}
