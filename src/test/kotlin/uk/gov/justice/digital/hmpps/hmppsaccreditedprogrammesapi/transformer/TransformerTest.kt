package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.UUID
import kotlin.time.Duration

class TransformerTest {
  @Test
  fun `transform course entity to api missing description and no prerequisites`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      prerequisites = emptySet(),
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
      prerequisites = emptySet(),
    )

    with(entity.toApi()) {
      description shouldBe entity.description
    }
  }

  @Test
  fun `transform course entity to api with prerequisites`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      prerequisites = setOf(
        Prerequisite(name = "gender", description = "female"),
        Prerequisite(name = "risk score", description = "ORGS: 50+"),
      ),
    )

    with(entity.toApi()) {
      coursePrerequisites shouldContainExactlyInAnyOrder listOf(
        CoursePrerequisite(name = "gender", description = "female"),
        CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
      )
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
      duration = Duration.parseIsoString("P5D"),
      groupSize = 5,
      contactEmail = "nobody-bwn@digital.justice.gov.uk",
      course = CourseEntity(
        id = UUID.randomUUID(),
        name = "A Course",
        type = "A type",
        prerequisites = emptySet(),
      ),
    )

    with(offering.toApi()) {
      id shouldBe offering.id
      organisationId shouldBe offering.organisationId
      Duration.parseIsoString(duration) shouldBe offering.duration
      groupSize shouldBe offering.groupSize
      contactEmail shouldBe offering.contactEmail
    }
  }
}
