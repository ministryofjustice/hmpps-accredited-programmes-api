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

private const val COURSE_JPA_REPO_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo"
private const val COURSE_DOMAIN_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain"
private const val REFERRAL_JPA_REPO_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.jparepo"
private const val REFERRAL_DOMAIN_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain"

@DataJpaTest
@ContextConfiguration(classes = [RepositoryTest::class])
@ComponentScan(basePackages = [COURSE_JPA_REPO_PACKAGE, REFERRAL_JPA_REPO_PACKAGE])
@EnableJpaRepositories(basePackages = [COURSE_JPA_REPO_PACKAGE, REFERRAL_JPA_REPO_PACKAGE])
@EntityScan(basePackages = [COURSE_DOMAIN_PACKAGE, REFERRAL_DOMAIN_PACKAGE])
@ActiveProfiles("test")
abstract class RepositoryTest(
  val jdbcTemplate: JdbcTemplate,
) {
  @BeforeEach
  fun truncateTables() {
    JdbcTestUtils.deleteFromTables(
      jdbcTemplate,
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
