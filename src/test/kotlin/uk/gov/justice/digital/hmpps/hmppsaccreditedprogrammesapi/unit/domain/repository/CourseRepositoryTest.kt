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
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PrerequisiteEntityFactory
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class CourseRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @MockBean
  private lateinit var auditService: AuditService

  @Test
  fun `CourseRepository should save and retrieve CourseEntity objects`() {
    var courseEntity = CourseEntityFactory().produce()
    courseEntity = entityManager.merge(courseEntity)

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
    var course = CourseEntityFactory()
      .withPrerequisites(prerequisites)
      .produce()
    course = entityManager.merge(course)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    persistedCourse.prerequisites shouldContainExactly prerequisites
  }

  @Test
  fun `CourseRepository should persist multiple OfferingEntity objects for multiple CourseEntity objects and verify ids`() {
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

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
    course = entityManager.merge(course)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    persistedCourse.offerings shouldHaveSize 3
    persistedCourse.offerings.forEach { it.id.shouldNotBeNull() }
  }

  @Test
  fun `CourseRepository should retrieve CourseEntity objects by their associated offering id`() {
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@a.com").produce()
    offering.course = course
    offering = entityManager.merge(offering)

    val persistedCourse = entityManager.find(CourseEntity::class.java, course.id)
    val persistedOffering = entityManager.find(OfferingEntity::class.java, offering.id)

    persistedCourse shouldBe course
    persistedOffering shouldBe offering
  }
}
