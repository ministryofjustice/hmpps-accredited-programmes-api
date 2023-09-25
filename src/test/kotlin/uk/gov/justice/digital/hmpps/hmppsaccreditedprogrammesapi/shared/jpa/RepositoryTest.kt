package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.jpa

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.TEST_USER_NAME

private const val BASE_PACKAGE = "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi"

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
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

  @BeforeEach
  fun setSecurityContextAuthentication() {
    SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(User(TEST_USER_NAME, "", emptyList()), null)
  }
}

fun commitAndStartNewTx() {
  TestTransaction.flagForCommit()
  TestTransaction.end()
  TestTransaction.start()
}
