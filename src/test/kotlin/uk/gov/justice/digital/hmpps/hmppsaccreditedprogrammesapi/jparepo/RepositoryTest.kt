package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
abstract class RepositoryTest(
  val entityManager: EntityManager,
) {
  @BeforeEach
  fun tearDownDb() {
    entityManager.createNativeQuery("DELETE FROM course_prerequisite").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM prerequisite").executeUpdate()
  }
}
