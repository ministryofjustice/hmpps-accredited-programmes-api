package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity

class CourseAudienceRelationshipTest
@Autowired
constructor(
  val courseRepository: CourseEntityRepository,
  val audienceRepository: AudienceRepository,
  entityManager: EntityManager,
) : RepositoryTest(entityManager) {
  @Test
  fun `can add audience values to courses`() {
    courseRepository.saveAll(
      listOf(
        CourseEntity(name = "Course 1", description = "A course", type = "Programme"),
        CourseEntity(name = "Course 2", description = "Another course", type = "Programme"),
        CourseEntity(name = "Course 3", description = "Yet another course", type = "Programme"),
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
