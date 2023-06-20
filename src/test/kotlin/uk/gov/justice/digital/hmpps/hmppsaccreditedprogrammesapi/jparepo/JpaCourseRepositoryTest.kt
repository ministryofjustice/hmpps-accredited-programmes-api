package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity

class JpaCourseRepositoryTest
@Autowired
constructor(
  val repository: JpaCourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `clear does not violate the unique constraint on audience_value`() {
    repository.saveCourse(
      CourseEntity(
        name = "Course 1",
        description = "A course",
        audiences = mutableSetOf(
          Audience("Male"),
          Audience("Female"),
        ),
      ),
    )

    commitAndStartNewTx()

    repository.clear()

    repository.saveCourse(
      CourseEntity(
        name = "Course 1",
        description = "A course",
        audiences = mutableSetOf(
          Audience("Male"),
          Audience("Female"),
        ),
      ),
    )

    commitAndStartNewTx()
  }
}
