package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

@DataJpaTest
@ContextConfiguration(classes = [CourseEntityRepositoryTest::class])
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain")
@ActiveProfiles("test")
class CourseEntityRepositoryTest
@Autowired
constructor(
  val repository: CourseEntityRepository,
  val jdbcTemplate: JdbcTemplate,
) {

  @BeforeEach
  fun truncateTables() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "prerequisite", "offering", "course")
    commitAndStartNewTx()
  }

  @Test
  fun `save and load behaves as expected`() {
    val transientEntity = CourseEntity(
      name = "A Course",
      type = "Approved Programme",
      description = "A representative Approved Programme for testing",
    )

    transientEntity.id.shouldBeNull()

    val persistentEntity = repository.save(transientEntity)
    persistentEntity.id.shouldNotBeNull()

    commitAndStartNewTx()

    val courses: Iterable<CourseEntity> = repository.findAll()
    courses shouldHaveSize 1

    val retrievedCourse = courses.first()
    retrievedCourse.shouldBeEqualToIgnoringFields(persistentEntity, CourseEntity::prerequisites, CourseEntity::audiences)

    repository.deleteAll()
  }

  @Test
  fun `persist course with prerequisites`() {
    val samples = setOf(
      Prerequisite("PR1", "PR1 D1"),
      Prerequisite("PR1", "PR1 D2"),
      Prerequisite("PR2", "PR2 D1"),
    )

    val course = CourseEntity(
      name = "A Course",
      type = "Approved Programme",
      description = "A representative Approved Programme for testing",
    ).apply {
      prerequisites.addAll(samples)
    }

    repository.save(course)

    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 3

    val persistentPrereqs = repository.findAll().first().prerequisites

    persistentPrereqs shouldContainExactly samples
    persistentPrereqs.remove(Prerequisite("PR1", "PR1 D2"))

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 2
    repository.delete(repository.findAll().first())

    commitAndStartNewTx()
    countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 0
    countRowsInTable(jdbcTemplate, "course") shouldBe 0
  }

  @Test
  fun `offering life-cycle`() {
    val course1 = CourseEntity(
      name = "A Course",
      type = "T",
      description = "A description",
    ).apply {
      offerings.add(Offering(organisationId = "BWI", contactEmail = "bwi@a.com"))
      offerings.add(Offering(organisationId = "MDI", contactEmail = "mdi@a.com"))
      offerings.add(Offering(organisationId = "BXI", contactEmail = "bxi@a.com"))
    }
    val course2 = CourseEntity(name = "Another Course", type = "T", description = "Another description")
      .apply {
        offerings.add(Offering(organisationId = "MDI", contactEmail = "mdi@a.com"))
      }

    repository.save(course1)
    repository.save(course2)
    commitAndStartNewTx()

    countRowsInTable(jdbcTemplate, "offering") shouldBe 4
    val persistentCourse = repository.findById(course1.id!!).orElseThrow()
    persistentCourse.offerings shouldHaveSize 3
    persistentCourse.offerings.shouldForAll { it.id.shouldNotBeNull() }
  }
}

fun commitAndStartNewTx() {
  TestTransaction.flagForCommit()
  TestTransaction.end()
  TestTransaction.start()
}
