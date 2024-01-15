package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AudienceEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PrerequisiteEntityFactory

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class CourseRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `CourseRepository should save and retrieve CourseEntity objects`() {
    val courseEntity = CourseEntityFactory().produce()
    entityManager.merge(courseEntity)

    val persistedCourse = entityManager.find(CourseEntity::class.java, courseEntity.id)
    persistedCourse shouldBe courseEntity
  }

  @Test
  fun `CourseRepository should persist a CourseEntity object with prerequisites`() {
    val prerequisites = mutableSetOf(
      PrerequisiteEntityFactory().withName("PR1").withDescription("PR1 D1").produce(),
      PrerequisiteEntityFactory().withName("PR1").withDescription("PR1 D2").produce(),
      PrerequisiteEntityFactory().withName("PR2").withDescription("PR2 D1").produce(),
    )
    val course = CourseEntityFactory()
      .withPrerequisites(prerequisites)
      .produce()
    entityManager.merge(course)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    persistedCourse.prerequisites shouldContainExactly prerequisites
  }

  @Test
  fun `CourseRepository should persist multiple OfferingEntity objects for multiple CourseEntity objects and verify ids`() {
    val course = CourseEntityFactory().produce()
    entityManager.merge(course)

    val offering1 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@a.com").produce()
    val offering2 = OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@a.com").produce()
    val offering3 = OfferingEntityFactory().withOrganisationId("BXI").withContactEmail("bxi@a.com").produce()

    offering1.course = course
    offering2.course = course
    offering3.course = course
    entityManager.merge(offering1)
    entityManager.merge(offering2)
    entityManager.merge(offering3)

    course.offerings.addAll(listOf(offering1, offering2, offering3))
    entityManager.merge(course)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    persistedCourse.offerings shouldHaveSize 3
    persistedCourse.offerings.forEach { it.id.shouldNotBeNull() }
  }

  @Test
  fun `CourseRepository should retrieve CourseEntity objects by their associated offering id`() {
    val course = CourseEntityFactory().produce()
    entityManager.merge(course)

    val offering = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@a.com").produce()
    offering.course = course
    entityManager.merge(offering)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    val persistedOffering = entityManager.find(OfferingEntity::class.java, offering.id)

    persistedCourse shouldBe course
    persistedOffering shouldBe offering
  }

  @Test
  fun `CourseRepository should add AudienceEntity values to CourseEntity objects`() {
    val courses = listOf(
      CourseEntityFactory().withName("Course 1").withIdentifier("C1").produce(),
      CourseEntityFactory().withName("Course 2").withIdentifier("C2").produce(),
      CourseEntityFactory().withName("Course 3").withIdentifier("C3").produce(),
    )

    val audiences = listOf(
      AudienceEntityFactory().withValue("Male").produce(),
      AudienceEntityFactory().withValue("Female").produce(),
    )

    courses.forEach(entityManager::merge)
    audiences.forEach(entityManager::merge)

    courses[0].audiences.add(audiences[0])
    courses[1].audiences.addAll(audiences)
    courses[2].audiences.add(audiences[1])

    val persistedCourses = entityManager.createQuery("SELECT c FROM CourseEntity c", CourseEntity::class.java).resultList
    persistedCourses.forEach { it.audiences.shouldNotBeNull() }
  }
}
