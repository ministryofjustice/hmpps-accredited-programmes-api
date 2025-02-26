package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test-h2")
class ReferralRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `ReferralRepository should save and retrieve ReferralEntity objects`() {
    var course = CourseEntityFactory().withId(null).produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().withId(null).produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory()
      .withUsername(REFERRER_USERNAME)
      .produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    val persistedReferral = entityManager.find(ReferralEntity::class.java, referral.id)
    persistedReferral shouldNotBe null
    persistedReferral.run {
      this.referrer shouldBe referrer
      this.prisonNumber shouldBe PRISON_NUMBER_1
      this.offering shouldBe offering
      this.offering.course.id shouldBe course.id
      this.oasysConfirmed shouldBe false
      this.hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `ReferralRepository should update and retrieve ReferralEntity objects`() {
    var course = CourseEntityFactory().withId(null).produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().withId(null).produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory()
      .withUsername(REFERRER_USERNAME)
      .produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering)
      .withOasysConfirmed(false)
      .produce()
    referral = entityManager.merge(referral)

    val persistedReferral = entityManager.find(ReferralEntity::class.java, referral.id)
    persistedReferral shouldNotBe null
    persistedReferral.run {
      this.referrer shouldBe referrer
      this.prisonNumber shouldBe PRISON_NUMBER_1
      this.offering shouldBe offering
      this.offering.course.id shouldBe course.id
      this.oasysConfirmed shouldBe false
    }

    persistedReferral.oasysConfirmed = true
    entityManager.merge(persistedReferral)

    val updatedReferral = entityManager.find(ReferralEntity::class.java, referral.id)
    updatedReferral shouldNotBe null
    updatedReferral.run {
      this.oasysConfirmed shouldBe true
    }
  }
}
