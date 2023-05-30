package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity

@SpringBootTest
@Transactional
class CourseEntityRepositoryTest(
  @Autowired
  val repository: CourseEntityRepository,
) {

  @Test
  fun `save and load behaves as expected`() {
    val transientEntity = CourseEntity(
      name = "A Course",
      type = "Approved Programme",
      description = "A representative Approved Programme for testing",
    )

    transientEntity.id.shouldBeNull()

    val persistentEntity = repository.save(transientEntity)
    persistentEntity.id.shouldNotBeNull()

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val courses: Iterable<CourseEntity> = repository.findAll()
    courses shouldHaveSize 1

    val retrievedCourse = courses.first()
    retrievedCourse.shouldBeEqualToIgnoringFields(persistentEntity, CourseEntity::prerequisites, CourseEntity::audiences)

    repository.deleteAll()
  }
}
