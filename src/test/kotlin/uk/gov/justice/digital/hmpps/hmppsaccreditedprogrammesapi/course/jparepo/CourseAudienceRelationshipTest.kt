package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.jpa.RepositoryTest

class CourseAudienceRelationshipTest
@Autowired
constructor(
  val courseRepository: CourseEntityRepository,
  val audienceRepository: AudienceRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `can add audience values to courses`() {
    courseRepository.saveAll(
      listOf(
        CourseEntity(name = "Course 1", identifier = "C1", description = "A course"),
        CourseEntity(name = "Course 2", identifier = "C2", description = "Another course"),
        CourseEntity(name = "Course 3", identifier = "C3", description = "Yet another course"),
      ),
    )

    audienceRepository.saveAll(
      listOf(
        Audience("Male"),
        Audience("Female"),
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val courses = courseRepository.findAll().toList()
    val audiences = audienceRepository.findAll().toList()

    courses[0].audiences.add(audiences[0])
    courses[1].audiences.addAll(audiences)
    courses[2].audiences.add(audiences[1])

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    audienceRepository.count() shouldBe 2
    courseRepository.count() shouldBe 3

    val courseAudiences = courseRepository
      .findAll()
      .fold(emptySet<Audience>()) { acc, course -> acc.plus(course!!.audiences) }

    courseAudiences shouldHaveSize 2
    courseAudiences.map { it.value } shouldBe setOf("Male", "Female")
  }
}
