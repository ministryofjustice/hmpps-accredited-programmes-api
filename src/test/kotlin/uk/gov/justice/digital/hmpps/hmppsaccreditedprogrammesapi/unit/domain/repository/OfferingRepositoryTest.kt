package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class OfferingRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `OfferingRepository should retrieve the correct offering for a CourseEntity object given a valid offeringId`(isWithdrawn: Boolean) {
    val course = CourseEntityFactory().produce()
    entityManager.merge(course)

    val offering = OfferingEntityFactory().withWithdrawn(isWithdrawn).produce()
    offering.course = course
    entityManager.merge(offering)

    val persistedOfferings = entityManager
      .createQuery("SELECT o FROM OfferingEntity o WHERE o.course.id = :courseId", OfferingEntity::class.java)
      .setParameter("courseId", course.id)
      .resultList

    persistedOfferings.first().withdrawn shouldBe isWithdrawn
    persistedOfferings.first().course.id shouldBe course.id
  }

  @Test
  fun `Given an offering that is subsequently replaced by another, OfferingRepository should return both the new and withdrawn offerings`() {
    val course = CourseEntityFactory().produce()
    entityManager.merge(course)

    val offeringWithdrawnFalse = OfferingEntityFactory().withWithdrawn(false).produce()
    offeringWithdrawnFalse.course = course
    entityManager.merge(offeringWithdrawnFalse)

    val offeringWithdrawnTrue = OfferingEntityFactory().withWithdrawn(true).produce()
    offeringWithdrawnTrue.course = course
    entityManager.merge(offeringWithdrawnTrue)

    val persistedOfferings = entityManager
      .createQuery("SELECT o FROM OfferingEntity o WHERE o.course.id = :courseId", OfferingEntity::class.java)
      .setParameter("courseId", course.id)
      .resultList

    persistedOfferings shouldContainExactlyInAnyOrder listOf(offeringWithdrawnFalse, offeringWithdrawnTrue)
    persistedOfferings.forEach { it.course.id shouldBe course.id }
  }
}
