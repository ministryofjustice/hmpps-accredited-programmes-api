package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience

class JpaCourseRepositoryTest
@Autowired
constructor(
  val repository: JpaCourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `can delete and re-insert without violating the unique database constraint on Audience values`() {
    repository.saveAudiences(
      setOf(
        Audience("Male"),
        Audience("Female"),
      ),
    )

    commitAndStartNewTx()

    repository.clear()
    repository.saveAudiences(
      setOf(
        Audience("Male"),
        Audience("Female"),
      ),
    )

    commitAndStartNewTx()
  }
}
