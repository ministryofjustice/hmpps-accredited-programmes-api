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
  fun `transform course entity to api missing description and no prerequisites`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      prerequisites = emptyList(),
      audiences = emptyList(),
    )

    with(entity.toApi()) {
      id shouldBe entity.id
      name shouldBe entity.name
      type shouldBe entity.type
      description shouldBe null
      coursePrerequisites.shouldBeEmpty()
    }
  }

  @Test
  fun `transform course entity to api with description`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      description = "A description",
      prerequisites = emptyList(),
      audiences = emptyList(),
    )

    with(entity.toApi()) {
      description shouldBe entity.description
    }
  }

  @Test
  fun `transform course entity to api with prerequisites and audience`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      prerequisites = listOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
      ),
      audiences = listOf(
        Audience("A"),
        Audience("B"),
        Audience("C"),
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
      id = UUID.randomUUID(),
      organisationId = "BXI",
      contactEmail = "nobody-bwn@digital.justice.gov.uk",
      course = CourseEntity(
        id = UUID.randomUUID(),
        name = "A Course",
        type = "A type",
        prerequisites = emptyList(),
        audiences = emptyList(),
      ),
    )

    with(offering.toApi()) {
      id shouldBe offering.id
      organisationId shouldBe offering.organisationId
      contactEmail shouldBe offering.contactEmail
    }
  }

  @Test
  fun `transform domain Audience to a CourseAudience`() {
    val audience = Audience("An audience")
    with(audience.toApi()) {
      id shouldBe audience.id
      value shouldBe audience.value
    }
  }
}
