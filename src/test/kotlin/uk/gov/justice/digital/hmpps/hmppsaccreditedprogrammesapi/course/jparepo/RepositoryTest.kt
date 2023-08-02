package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

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

private const val JPA_REPO_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo"
private const val DOMAIN_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain"

@DataJpaTest
@ContextConfiguration(classes = [RepositoryTest::class])
@ComponentScan(basePackages = [JPA_REPO_PACKAGE])
@EnableJpaRepositories(basePackages = [JPA_REPO_PACKAGE])
@EntityScan(DOMAIN_PACKAGE)
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
