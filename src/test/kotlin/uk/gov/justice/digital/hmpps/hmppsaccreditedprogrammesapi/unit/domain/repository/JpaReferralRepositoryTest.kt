package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.UUID

class JpaReferralRepositoryTest
@Autowired
constructor(
  val referralRepository: JpaReferralRepository,
  val courseRepository: CourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {
  @Test
  fun `JpaReferralRepository should save and retrieve ReferralEntity objects`() {
    val persistentOfferingId = persistAnOffering()
    val prisonNumber = randomPrisonNumber()
    val referrerId = randomUppercaseAlphanumericString(10)

    val referral = ReferralEntityFactory()
      .withReferrerId(referrerId)
      .withPrisonNumber(prisonNumber)
      .withOfferingId(persistentOfferingId)
      .produce()

    val referralId = referralRepository.save(referral).id!!

    commitAndStartNewTx()

    referralRepository.findById(referralId) shouldBePresent {
      referrerId shouldBe referrerId
      prisonNumber shouldBe prisonNumber
      offeringId shouldBe persistentOfferingId
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `JpaReferralRepository should update and retrieve ReferralEntity objects`() {
    val persistentOfferingId = persistAnOffering()
    val prisonNumber = randomPrisonNumber()
    val referrerId = randomUppercaseAlphanumericString(10)

    val referral = ReferralEntityFactory().withReferrerId(referrerId).withPrisonNumber(prisonNumber).withOfferingId(persistentOfferingId).produce()

    val referralId = referralRepository.save(referral).id!!

    commitAndStartNewTx()

    val persistentReferral = referralRepository.findById(referralId).get()
    with(persistentReferral) {
      oasysConfirmed = true
      hasReviewedProgrammeHistory = true
    }

    commitAndStartNewTx()

    referralRepository.findById(referralId) shouldBePresent {
      referrerId shouldBe referrerId
      prisonNumber shouldBe prisonNumber
      offeringId shouldBe persistentOfferingId
      oasysConfirmed shouldBe true
      hasReviewedProgrammeHistory shouldBe true
    }
  }

  private fun persistAnOffering(): UUID {
    val courseIdentifier = randomLowercaseString(6)

    val course = CourseEntityFactory()
      .withIdentifier(courseIdentifier)
      .withName(randomSentence(1..3, 1..8))
      .withAlternateName(null)
      .produce()

    val offering = OfferingEntityFactory()
      .withOrganisationId(randomUppercaseString(3))
      .withContactEmail(randomEmailAddress())
      .produce()

    courseRepository.saveCourse(course.apply { addOffering(offering) })
    commitAndStartNewTx()
    return courseRepository.getAllCourses()[0].offerings.first().id!!
  }
}
