package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@ActiveProfiles("test")
abstract class RepositoryTestBase(
  val jdbcTemplate: JdbcTemplate,
) {
  @BeforeEach
  fun truncateTables() {
    JdbcTestUtils.deleteFromTables(
      jdbcTemplate,
      "course_participation",
      "referral",
      "prerequisite",
      "offering",
      "course_audience",
      "audience",
      "course",
    )
    commitAndStartNewTx()
  }
}

fun commitAndStartNewTx() {
  TestTransaction.flagForCommit()
  TestTransaction.end()
  TestTransaction.start()
}
