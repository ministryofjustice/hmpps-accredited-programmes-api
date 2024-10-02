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
@ActiveProfiles("test-h2")
class OfferingRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `OfferingRepository should retrieve the correct offering for a CourseEntity object given a valid offeringId`(isWithdrawn: Boolean) {
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().withWithdrawn(isWithdrawn).produce()
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
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offeringWithdrawnFalse = OfferingEntityFactory().withWithdrawn(false).produce()
    offeringWithdrawnFalse.course = course
    offeringWithdrawnFalse = entityManager.merge(offeringWithdrawnFalse)

    var offeringWithdrawnTrue = OfferingEntityFactory().withWithdrawn(true).produce()
    offeringWithdrawnTrue.course = course
    offeringWithdrawnTrue = entityManager.merge(offeringWithdrawnTrue)

    val persistedOfferings = entityManager
      .createQuery("SELECT o FROM OfferingEntity o WHERE o.course.id = :courseId", OfferingEntity::class.java)
      .setParameter("courseId", course.id)
      .resultList

    persistedOfferings shouldContainExactlyInAnyOrder listOf(offeringWithdrawnFalse, offeringWithdrawnTrue)
    persistedOfferings.forEach { it.course.id shouldBe course.id }
  }
}
