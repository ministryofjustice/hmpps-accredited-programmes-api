package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

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

@DataJpaTest
@ContextConfiguration(classes = [RepositoryTest::class])
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo"])
@ComponentScan(basePackages = ["uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain")
@ActiveProfiles("test")
abstract class RepositoryTest(
  val jdbcTemplate: JdbcTemplate,
) {
  @BeforeEach
  fun truncateTables() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "prerequisite", "offering", "course_audience", "audience", "course")
    commitAndStartNewTx()
  }
}

fun commitAndStartNewTx() {
  TestTransaction.flagForCommit()
  TestTransaction.end()
  TestTransaction.start()
}
