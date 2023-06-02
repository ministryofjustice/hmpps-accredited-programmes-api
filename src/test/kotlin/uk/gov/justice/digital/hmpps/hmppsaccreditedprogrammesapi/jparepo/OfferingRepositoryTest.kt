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

  @Test
  fun `find by courseId`() {
    val c1 = CourseEntity(name = "Course 1", type = "Accredited Programme", description = "A Course 1")
    val c2 = CourseEntity(name = "Course 1", type = "Accredited Programme", description = "A Course 2")

    courseRepo.saveAll(listOf(c1, c2))

    val o1 = Offering(organisationId = "MDI", contactEmail = "a@b.com", course = c1)
    val o2 = Offering(organisationId = "BXI", contactEmail = "a@b.com", course = c1)
    val o3 = Offering(organisationId = "MDI", contactEmail = "a@b.com", course = c2)

    offeringRepo.saveAll(listOf(o1, o2, o3))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    courseRepo.findAll() shouldHaveSize 2
    offeringRepo.findAll() shouldHaveSize 3

    offeringRepo.findByCourseId(c1.id!!) shouldBe setOf(o1, o2)
    offeringRepo.findByCourseId(c2.id!!) shouldBe setOf(o3)
  }
}
