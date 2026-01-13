package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

@Transactional
class OfferingRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var entityManager: EntityManager

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `OfferingRepository should retrieve the correct offering for a CourseEntity object given a valid offeringId`(isWithdrawn: Boolean) {
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().withWithdrawn(isWithdrawn).withOrganisationId("MDI").produce()
    offering.course = course
    entityManager.merge(offering)

    val persistedOfferings = entityManager
      .createQuery("SELECT o FROM OfferingEntity o WHERE o.course.id = :courseId", OfferingEntity::class.java)
      .setParameter("courseId", course.id)
      .resultList

    persistedOfferings.first().withdrawn shouldBe isWithdrawn
    persistedOfferings.first().course.id shouldBe course.id
  }
}
