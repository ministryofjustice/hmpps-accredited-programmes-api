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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OverrideType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.EligibilityOverrideReasonEntityFactory
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
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
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
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
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

  @Test
  fun `A referral should be able to access its eligibility override reasons`() {
    // Given
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory().produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    // When
    var eligibilityOverrideReasonEntity = EligibilityOverrideReasonEntityFactory().withId(null).withReferral(referral).produce()
    eligibilityOverrideReasonEntity = entityManager.merge(eligibilityOverrideReasonEntity)
    entityManager.flush()
    entityManager.clear()

    // Then
    val referralEntity = entityManager.find(ReferralEntity::class.java, referral.id)
    referralEntity.eligibilityOverrideReasons.size shouldBe 1
    referralEntity.eligibilityOverrideReasons.first().id shouldBe eligibilityOverrideReasonEntity.id
    referralEntity.eligibilityOverrideReasons.first().reason shouldBe "Test override reason"
    referralEntity.eligibilityOverrideReasons.first().overrideType shouldBe OverrideType.HEALTHY_SEX_PROGRAMME
  }
}
