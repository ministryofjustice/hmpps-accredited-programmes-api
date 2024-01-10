package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory

class ReferralRepositoryTest
@Autowired
constructor(
  val referralRepository: ReferralRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `ReferralRepository should save and retrieve ReferralEntity objects`() {
    val course = CourseEntityFactory()
      .withIdentifier(randomLowercaseString(6))
      .withName(randomSentence(1..3, 1..8))
      .withAlternateName(null)
      .produce()

    val offering = OfferingEntityFactory()
      .withOrganisationId(randomUppercaseString(3))
      .withContactEmail(randomEmailAddress())
      .produce()

    entityManager.merge(course.apply { addOffering(offering) })

    val referrer = ReferrerUserEntityFactory()
      .withUsername(CLIENT_USERNAME)
      .produce()

    entityManager.persist(referrer)

    val referral = ReferralEntityFactory()
      .withReferrer(referrer)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering)
      .produce()

    entityManager.merge(referral)

    referralRepository.findById(referral.id!!) shouldBePresent {
      referrer shouldBe referrer
      prisonNumber shouldBe prisonNumber
      offering shouldBe offering
      offering.course shouldBe course
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `ReferralRepository should update and retrieve ReferralEntity objects`() {
    val course = CourseEntityFactory()
      .withIdentifier(randomLowercaseString(6))
      .withName(randomSentence(1..3, 1..8))
      .withAlternateName(null)
      .produce()

    val offering = OfferingEntityFactory()
      .withOrganisationId(randomUppercaseString(3))
      .withContactEmail(randomEmailAddress())
      .produce()

    entityManager.merge(course.apply { addOffering(offering) })

    val referrer = ReferrerUserEntityFactory()
      .withUsername(CLIENT_USERNAME)
      .produce()

    entityManager.persist(referrer)

    val referral = ReferralEntityFactory()
      .withReferrer(referrer)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering)
      .produce()

    entityManager.merge(referral)

    referral.oasysConfirmed = true
    referral.hasReviewedProgrammeHistory = true

    entityManager.merge(referral)

    referralRepository.findById(referral.id!!) shouldBePresent {
      referrer shouldBe referrer
      prisonNumber shouldBe prisonNumber
      offering shouldBe offering
      offering.course shouldBe course
      oasysConfirmed shouldBe true
      hasReviewedProgrammeHistory shouldBe true
    }
  }
}
