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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaReferralRepository
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
    val referralId = referralRepository.save(ReferralEntity(referrerId = referrerId, prisonNumber = prisonNumber, offeringId = persistentOfferingId)).id!!

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
    val referralId = referralRepository.save(ReferralEntity(referrerId = referrerId, prisonNumber = prisonNumber, offeringId = persistentOfferingId)).id!!

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
    courseRepository.saveCourse(
      CourseEntity(
        identifier = courseIdentifier,
        name = randomSentence(1..3, 1..8),
        alternateName = null,
      ).apply {
        addOffering(OfferingEntity(organisationId = randomUppercaseString(3), contactEmail = randomEmailAddress()))
      },
    )
    commitAndStartNewTx()
    return courseRepository.getAllCourses()[0].offerings.first().id!!
  }
}
