package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaAudienceRepository

class JpaAudienceRepositoryTest
@Autowired
constructor(
  val audienceRepository: JpaAudienceRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {
  @Test
  fun `JpaAudienceRepository successfully saves and retrieves AudienceEntity objects`() {
    val transientAudience = AudienceEntity("A")
    audienceRepository.save(transientAudience)

    TestTransaction.flagForCommit()
    TestTransaction.end()

    transientAudience.id.shouldNotBeNull()

    TestTransaction.start()

    val audiences = audienceRepository.findAll()

    audiences shouldHaveSize 1
    audiences shouldContainExactly setOf(AudienceEntity(value = "A", transientAudience.id))
  }

  @Test
  fun `JpaAudienceRepository ignores duplicate AudienceEntity objects on persist`() {
    val a1 = AudienceEntity("A")
    val a2 = AudienceEntity("A")
    audienceRepository.saveAll(listOf(a1, a2))

    TestTransaction.flagForCommit()
    shouldThrow<DataIntegrityViolationException> {
      TestTransaction.end()
    }
  }
}
