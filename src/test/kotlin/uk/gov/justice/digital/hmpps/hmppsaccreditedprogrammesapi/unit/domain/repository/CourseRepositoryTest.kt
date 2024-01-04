package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import kotlin.jvm.optionals.getOrNull

class CourseRepositoryTest
@Autowired
constructor(
  val courseRepository: CourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {

  @Autowired
  private lateinit var courseService: CourseService

  @Test
  fun `CourseRepository should save and retrieve CourseEntity objects`() {
    val transientEntity = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A representative Approved Programme for testing",
    )

    transientEntity.id.shouldBeNull()

    val persistentEntity = courseRepository.save(transientEntity)
    persistentEntity.id.shouldNotBeNull()

    commitAndStartNewTx()

    val courses: Iterable<CourseEntity> = courseRepository.findAll()
    courses shouldHaveSize 1

    val retrievedCourse = courses.first()
    retrievedCourse.shouldBeEqualToIgnoringFields(persistentEntity, CourseEntity::prerequisites, CourseEntity::audiences)

    courseRepository.deleteAll()
  }

  @Test
  fun `CourseRepository should persist a CourseEntity object with prerequisites`() {
    val samples = setOf(
      PrerequisiteEntity(name = "PR1", description = "PR1 D1"),
      PrerequisiteEntity(name = "PR1", description = "PR1 D2"),
      PrerequisiteEntity(name = "PR2", description = "PR2 D1"),
    )

    val course = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A representative Approved Programme for testing",
    ).apply {
      prerequisites.addAll(samples)
    }

    courseRepository.save(course)

    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 3

    val persistentPrereqs = courseRepository.findAll().first().prerequisites

    persistentPrereqs shouldContainExactly samples
    persistentPrereqs.remove(PrerequisiteEntity(name = "PR1", description = "PR1 D2"))

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 2
    courseRepository.delete(courseRepository.findAll().first())

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 0
    countRowsInTable(jdbcTemplate, "course") shouldBe 0
  }

  @Test
  fun `CourseRepository should persist multiple OfferingEntity objects for multiple CourseEntity objects and verify ids`() {
    val course1 = CourseEntityFactory()
      .withName("A course")
      .withIdentifier("ID")
      .withDescription("A description")
      .produce()

    course1.id?.let {
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@a.com").produce())
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@a.com").produce())
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("BXI").withContactEmail("bxi@a.com").produce())
    }

    val course2 = CourseEntityFactory()
      .withName("Another course")
      .withIdentifier("ACANO")
      .withIdentifier("Another description")
      .produce()

    course2.id?.let {
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@a.com").produce())
    }

    courseRepository.save(course1)
    courseRepository.save(course2)
    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "offering") shouldBe 4
    val persistentCourse = courseRepository.findById(course1.id!!).getOrNull()
    persistentCourse?.mutableOfferings?.shouldHaveSize(3)
    persistentCourse?.mutableOfferings?.shouldForAll { it.id.shouldNotBeNull() }
  }

  @Test
  fun `CourseRepository should retrieve CourseEntity objects by their associated offering id`() {
    val course1 = CourseEntityFactory()
      .withName("A course")
      .withIdentifier("ID")
      .withDescription("A description")
      .produce()

    course1.id?.let {
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@a.com").produce())
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@a.com").produce())
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("BXI").withContactEmail("bxi@a.com").produce())
    }

    val course2 = CourseEntityFactory()
      .withName("Another course")
      .withIdentifier("ACANO")
      .withIdentifier("Another description")
      .produce()

    course2.id?.let {
      courseService.addOfferingToCourse(it, OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@a.com").produce())
    }

    courseRepository.save(course1)
    courseRepository.save(course2)
    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "offering") shouldBe 4
    val persistentCourse = courseRepository.findById(course1.id!!).getOrNull()
    val offeringId = persistentCourse?.mutableOfferings?.first()?.id!!
    val courseByOfferingIdInSameTx = courseRepository.findByMutableOfferingsId(offeringId)
    courseByOfferingIdInSameTx shouldBe persistentCourse

    commitAndStartNewTx()

    val courseByOfferingInNewTx = courseRepository.findByMutableOfferingsId(offeringId)
    courseByOfferingInNewTx shouldBe persistentCourse
  }
}
