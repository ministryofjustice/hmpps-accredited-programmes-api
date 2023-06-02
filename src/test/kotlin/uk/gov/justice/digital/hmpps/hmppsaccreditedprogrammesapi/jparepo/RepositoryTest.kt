package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TestTransaction

@DataJpaTest
@ContextConfiguration(classes = [RepositoryTest::class])
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain")
@ActiveProfiles("test")
abstract class RepositoryTest(
  val entityManager: EntityManager,
) {
  @BeforeEach
  fun tearDownDb() {
    with(entityManager) {
      listOf(
        createNativeQuery("DELETE FROM course_audience"),
        createNativeQuery("DELETE FROM audience"),
        createNativeQuery("DELETE FROM offering"),
        createNativeQuery("DELETE FROM course_prerequisite"),
        createNativeQuery("DELETE FROM course"),
        createNativeQuery("DELETE FROM prerequisite"),
      ).forEach(Query::executeUpdate)
    }
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()
  }
}
