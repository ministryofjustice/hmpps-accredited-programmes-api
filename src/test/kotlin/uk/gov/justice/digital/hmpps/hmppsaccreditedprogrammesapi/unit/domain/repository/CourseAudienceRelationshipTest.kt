package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AudienceRepository

class CourseAudienceRelationshipTest
@Autowired
constructor(
  val courseEntityRepository: CourseEntityRepository,
  val audienceRepository: AudienceRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {
  @Test
  fun `CourseEntityRepository should add AudienceEntity values to CourseEntity objects`() {
    courseEntityRepository.saveAll(
      listOf(
        CourseEntity(name = "Course 1", identifier = "C1", description = "A course"),
        CourseEntity(name = "Course 2", identifier = "C2", description = "Another course"),
        CourseEntity(name = "Course 3", identifier = "C3", description = "Yet another course"),
      ),
    )

    audienceRepository.saveAll(
      listOf(
        AudienceEntity("Male"),
        AudienceEntity("Female"),
      ),
    )

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val courses = courseEntityRepository.findAll().toList()
    val audiences = audienceRepository.findAll().toList()

    courses[0].audiences.add(audiences[0])
    courses[1].audiences.addAll(audiences)
    courses[2].audiences.add(audiences[1])

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    audienceRepository.count() shouldBe 2
    courseEntityRepository.count() shouldBe 3

    val courseAudiences = courseEntityRepository
      .findAll()
      .fold(emptySet<AudienceEntity>()) { acc, course -> acc.plus(course!!.audiences) }

    courseAudiences shouldHaveSize 2
    courseAudiences.map { it.value } shouldBe setOf("Male", "Female")
  }
}
