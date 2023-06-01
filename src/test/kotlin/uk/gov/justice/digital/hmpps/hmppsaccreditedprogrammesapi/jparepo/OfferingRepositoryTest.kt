package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering

class OfferingRepositoryTest
@Autowired constructor(
  val offeringRepo: OfferingRepository,
  val courseRepo: CourseEntityRepository,
  entityManager: EntityManager,
) : RepositoryTest(entityManager) {
  @Test
  fun `save and load behaves as expected`() {
    val course = CourseEntity(
      name = "Course",
      type = "Accredited Programme",
      description = "A Course",
    )

    courseRepo.save(course)

    val offering = Offering(
      organisationId = "MDI",
      contactEmail = "ap-admin@somewhere.x.y",
      course = course,
    )

    offeringRepo.save(offering)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val persistentOfferings = offeringRepo.findAll()
    persistentOfferings shouldHaveSize 1

    with(persistentOfferings.first()) {
      organisationId shouldBe offering.organisationId
      contactEmail shouldBe offering.contactEmail
      id.shouldNotBeNull()
      course.id.shouldNotBeNull()
    }
  }
}
