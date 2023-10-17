package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Prerequisite

class CourseEntityRepositoryTest
@Autowired
constructor(
  val courseEntityRepository: CourseEntityRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `courseEntityRepository successfully saves and retrieves records`() {
    val transientEntity = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A representative Approved Programme for testing",
    )

    transientEntity.id.shouldBeNull()

    val persistentEntity = courseEntityRepository.save(transientEntity)
    persistentEntity.id.shouldNotBeNull()

    commitAndStartNewTx()

    val courses: Iterable<CourseEntity> = courseEntityRepository.findAll()
    courses shouldHaveSize 1

    val retrievedCourse = courses.first()
    retrievedCourse.shouldBeEqualToIgnoringFields(persistentEntity, CourseEntity::prerequisites, CourseEntity::audiences)

    courseEntityRepository.deleteAll()
  }

  @Test
  fun `courseEntityRepository should successfully persist a course with prerequisites`() {
    val samples = setOf(
      Prerequisite("PR1", "PR1 D1"),
      Prerequisite("PR1", "PR1 D2"),
      Prerequisite("PR2", "PR2 D1"),
    )

    val course = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A representative Approved Programme for testing",
    ).apply {
      prerequisites.addAll(samples)
    }

    courseEntityRepository.save(course)

    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 3

    val persistentPrereqs = courseEntityRepository.findAll().first().prerequisites

    persistentPrereqs shouldContainExactly samples
    persistentPrereqs.remove(Prerequisite("PR1", "PR1 D2"))

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 2
    courseEntityRepository.delete(courseEntityRepository.findAll().first())

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 0
    countRowsInTable(jdbcTemplate, "course") shouldBe 0
  }

  @Test
  fun `courseEntityRepository should persist multiple offerings for multiple courses and validate offering ids`() {
    val course1 = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A description",
    ).apply {
      addOffering(OfferingEntity(organisationId = "BWI", contactEmail = "bwi@a.com"))
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
      addOffering(OfferingEntity(organisationId = "BXI", contactEmail = "bxi@a.com"))
    }
    val course2 = CourseEntity(name = "Another Course", identifier = "ACANO", description = "Another description")
      .apply {
        addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
      }

    courseEntityRepository.save(course1)
    courseEntityRepository.save(course2)
    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "offering") shouldBe 4
    val persistentCourse = courseEntityRepository.findById(course1.id!!).orElseThrow()
    persistentCourse.offerings shouldHaveSize 3
    persistentCourse.offerings.shouldForAll { it.id.shouldNotBeNull() }
  }

  @Test
  fun `courseEntityRepository should retrieve course by its associated offering id`() {
    val course1 = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A description",
    ).apply {
      addOffering(OfferingEntity(organisationId = "BWI", contactEmail = "bwi@a.com"))
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
      addOffering(OfferingEntity(organisationId = "BXI", contactEmail = "bxi@a.com"))
    }
    val course2 = CourseEntity(name = "Another Course", identifier = "ACANO", description = "Another description")
      .apply {
        addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
      }

    courseEntityRepository.save(course1)
    courseEntityRepository.save(course2)
    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "offering") shouldBe 4
    val persistentCourse = courseEntityRepository.findById(course1.id!!).orElseThrow()
    val offeringId = persistentCourse.offerings.first().id!!
    val courseByOfferingIdInSameTx = courseEntityRepository.findByMutableOfferings_id(offeringId)
    courseByOfferingIdInSameTx shouldBeSameInstanceAs persistentCourse

    commitAndStartNewTx()

    val courseByOfferingInNewTx = courseEntityRepository.findByMutableOfferings_id(offeringId)
    courseByOfferingInNewTx shouldNotBeSameInstanceAs persistentCourse
    courseByOfferingInNewTx shouldBe persistentCourse
  }
}
