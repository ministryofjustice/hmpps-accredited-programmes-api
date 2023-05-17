package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.PrerequisiteEntity
import java.util.UUID

class TransformerTest {
  @Test
  fun `transform course entity to api missing description and no prerequisites`() {
    val entity = CourseEntity(
      id = UUID.randomUUID(),
      name = "A Course",
      type = "A type",
      prerequisites = emptyList(),
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
      prerequisites = listOf(
        PrerequisiteEntity(name = "gender", description = "female"),
        PrerequisiteEntity(name = "risk score", description = "ORGS: 50+"),
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
    val entity = PrerequisiteEntity(
      name = "gender",
      description = "female",
    )

    with(entity.toApi()) {
      name shouldBe entity.name
      description shouldBe description
    }
  }
}
