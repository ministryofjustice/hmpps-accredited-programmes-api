package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils

private const val BASE_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi"

private const val COURSE_PACKAGE = "$BASE_PACKAGE.course"

@DataJpaTest
@ContextConfiguration(classes = [RepositoryTest::class])
@ComponentScan(basePackages = [COURSE_PACKAGE, "$BASE_PACKAGE.shared"])
@EnableJpaRepositories(basePackages = [BASE_PACKAGE])
@EntityScan(basePackages = [BASE_PACKAGE])
@ActiveProfiles("test")
abstract class RepositoryTest(
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
