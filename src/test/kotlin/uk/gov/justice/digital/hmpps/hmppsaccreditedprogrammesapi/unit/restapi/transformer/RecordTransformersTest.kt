package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.transformer

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PrerequisiteEntityFactory

class RecordTransformersTest {
  @Test
  fun `Transforming a course entity with all required fields should convert to its API equivalent`() {
    val p1 = PrerequisiteEntityFactory().withName("gender").withDescription("female").produce()
    val p2 = PrerequisiteEntityFactory().withName("risk score").withDescription("ORGS: 50+").produce()
    val prerequisites = mutableSetOf(p1, p2)
    val entity = CourseEntityFactory()
      .withPrerequisites(prerequisites)
      .produce()

    with(entity.toApi()) {
      coursePrerequisites shouldContainExactlyInAnyOrder listOf(
        CoursePrerequisite(name = "gender", description = "female"),
        CoursePrerequisite(name = "risk score", description = "ORGS: 50+"),
      )
    }
  }

  @Test
  fun `Transforming a course entity with missing fields should tolerantly convert`() {
    val entity = CourseEntityFactory().produce()

    with(entity.toApi()) {
      id shouldBe entity.id
      name shouldBe entity.name
      description shouldBe null
      alternateName shouldBe null
      coursePrerequisites.shouldBeEmpty()
    }
  }

  @Test
  fun `Transforming a course entity with empty prerequisites and audiences should tolerantly convert`() {
    val entity = CourseEntityFactory()
      .withDescription("A description")
      .withAlternateName("AA++")
      .produce()

    with(entity.toApi()) {
      description shouldBe entity.description
      alternateName shouldBe entity.alternateName
    }
  }

  @Test
  fun `Transforming a course prerequisite entity should convert to its API equivalent`() {
    val entity = PrerequisiteEntityFactory().produce()

    with(entity.toApi()) {
      name shouldBe entity.name
      description shouldBe entity.description
    }
  }

  @Test
  fun `Transforming an offering entity should convert to its API equivalent`() {
    val offering = OfferingEntityFactory()
      .withSecondaryContactEmail("nobody-bwn2@digital.justice.gov.uk")
      .produce()

    with(offering.toApi(true, "M")) {
      id shouldBe offering.id
      organisationId shouldBe offering.organisationId
      organisationEnabled shouldBe true
      contactEmail shouldBe offering.contactEmail
      secondaryContactEmail shouldBe offering.secondaryContactEmail
      referable shouldBe true
      gender shouldBe Gender.M
    }
  }
}
