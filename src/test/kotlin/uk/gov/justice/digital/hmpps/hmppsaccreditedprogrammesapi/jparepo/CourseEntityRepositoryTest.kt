package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

@SpringBootTest
@Transactional
class CourseEntityRepositoryTest
@Autowired
constructor(
  val repository: CourseEntityRepository,
  val jdbcTemplate: JdbcTemplate,
) {

  @BeforeEach
  fun truncateTables() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "prerequisite", "course")
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

    JdbcTestUtils.countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 3

    val persistentPrereqs = repository.findAll().first().prerequisites

    persistentPrereqs shouldContainExactly samples
    persistentPrereqs.remove(Prerequisite("PR1", "PR1 D2"))

    commitAndStartNewTx()
    JdbcTestUtils.countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 2
    repository.delete(repository.findAll().first())

    commitAndStartNewTx()
    JdbcTestUtils.countRowsInTable(jdbcTemplate, "prerequisite") shouldBe 0
    JdbcTestUtils.countRowsInTable(jdbcTemplate, "course") shouldBe 0
  }
}

fun commitAndStartNewTx() {
  TestTransaction.flagForCommit()
  TestTransaction.end()
  TestTransaction.start()
}
