package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Prerequisite
import java.util.UUID

class RecordTransformersTest {
  @Test
  fun `Transforming a course entity with all required fields should convert to its API equivalent`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      identifier = "AC",
      prerequisites = mutableSetOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
      ),
      audiences = mutableSetOf(
        Audience(value = "A", id = UUID.randomUUID()),
        Audience(value = "B", id = UUID.randomUUID()),
        Audience(value = "C", id = UUID.randomUUID()),
      ),
      referable = true,
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
  fun `Transforming a course entity with missing fields should tolerantly convert`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      identifier = "AC",
      prerequisites = mutableSetOf(),
      audiences = mutableSetOf(),
      referable = true,
    )

    with(entity.toApi()) {
      id shouldBe entity.id
      name shouldBe entity.name
      description shouldBe null
      alternateName shouldBe null
      coursePrerequisites.shouldBeEmpty()
      referable.shouldBe(true)
    }
  }

  @Test
  fun `Transforming a course entity with empty prerequisites and audiences should tolerantly convert`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      identifier = "AC",
      description = "A description",
      alternateName = "AA++",
      prerequisites = mutableSetOf(),
      audiences = mutableSetOf(),
      referable = true,
    )

    with(entity.toApi()) {
      description shouldBe entity.description
      alternateName shouldBe entity.alternateName
    }
  }

  @Test
  fun `Transforming a course prerequisite entity should convert to its API equivalent`() {
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
  fun `Transforming an offering entity should convert to its API equivalent`() {
    val offering = Offering(
      id = UUID.randomUUID(),
      organisationId = "BXI",
      contactEmail = "nobody-bwn@digital.justice.gov.uk",
      secondaryContactEmail = "nobody-bwn2@digital.justice.gov.uk",
    )

    with(offering.toApi()) {
      id shouldBe offering.id
      organisationId shouldBe offering.organisationId
      contactEmail shouldBe offering.contactEmail
      secondaryContactEmail shouldBe offering.secondaryContactEmail
    }
  }

  @Test
  fun `Transforming an audience entity should convert to its API equivalent`() {
    val audience = Audience(value = "An audience", id = UUID.randomUUID())
    with(audience.toApi()) {
      id shouldBe audience.id
      value shouldBe audience.value
    }
  }
}