package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

@SpringBootTest
@Transactional
class PrerequisiteRepositoryTest(
  @Autowired
  val repository: PrerequisiteRepository,
) {
  @Test
  fun `save and load behaves as expected`() {
    val transientEntity = Prerequisite(
      name = "Prerequisite 1",
      description = "One prerequisite",
    )

    transientEntity.id.shouldBeNull()

    val persistentEntity = repository.save(transientEntity)
    persistentEntity.id.shouldNotBeNull()

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    val prerequisites: Iterable<Prerequisite> = repository.findAll()
    prerequisites shouldHaveSize 1

    val retrievedEntity = prerequisites.first()
    retrievedEntity shouldBeEqualToComparingFields persistentEntity

    repository.deleteAll()
  }
}
