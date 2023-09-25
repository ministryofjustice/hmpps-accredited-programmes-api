package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience

class AudienceRepositoryTest
@Autowired
constructor(
  val repository: AudienceRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `audienceRepository successfully saves and retrieves records`() {
    val transientAudience = Audience("A")
    repository.save(transientAudience)

    TestTransaction.flagForCommit()
    TestTransaction.end()

    transientAudience.id.shouldNotBeNull()

    TestTransaction.start()

    val audiences = repository.findAll()

    audiences shouldHaveSize 1
    audiences shouldContainExactly setOf(Audience(value = "A", transientAudience.id))
  }

  @Test
  fun `audienceRepository ignores duplicate audience records when attempting to save`() {
    val a1 = Audience("A")
    val a2 = Audience("A")
    repository.saveAll(listOf(a1, a2))

    TestTransaction.flagForCommit()
    shouldThrow<DataIntegrityViolationException> {
      TestTransaction.end()
    }
  }
}
