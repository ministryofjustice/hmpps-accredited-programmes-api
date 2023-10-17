package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.repositories

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomUppercaseAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral
import java.util.UUID

class JpaReferralRepositoryTest
@Autowired
constructor(
  val repository: JpaReferralRepository,
  val courseRepository: CourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `referralRepository should successfully save and retrieve records`() {
    val persistentOfferingId = persistAnOffering()
    val prisonNumber = randomPrisonNumber()
    val referrerId = randomUppercaseAlphanumericString(10)
    val referralId = repository.save(Referral(referrerId = referrerId, prisonNumber = prisonNumber, offeringId = persistentOfferingId)).id!!

    commitAndStartNewTx()

    repository.findById(referralId) shouldBePresent {
      referrerId shouldBe referrerId
      prisonNumber shouldBe prisonNumber
      offeringId shouldBe persistentOfferingId
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `referralRepository should successfully update and retrieve records`() {
    val persistentOfferingId = persistAnOffering()
    val prisonNumber = randomPrisonNumber()
    val referrerId = randomUppercaseAlphanumericString(10)
    val referralId = repository.save(Referral(referrerId = referrerId, prisonNumber = prisonNumber, offeringId = persistentOfferingId)).id!!

    commitAndStartNewTx()

    val persistentReferral = repository.findById(referralId).get()
    with(persistentReferral) {
      oasysConfirmed = true
      hasReviewedProgrammeHistory = true
    }

    commitAndStartNewTx()

    repository.findById(referralId) shouldBePresent {
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
        addOffering(Offering(organisationId = randomUppercaseString(3), contactEmail = randomEmailAddress()))
      },
    )
    commitAndStartNewTx()
    return courseRepository.getAllCourses()[0].offerings.first().id!!
  }
}
