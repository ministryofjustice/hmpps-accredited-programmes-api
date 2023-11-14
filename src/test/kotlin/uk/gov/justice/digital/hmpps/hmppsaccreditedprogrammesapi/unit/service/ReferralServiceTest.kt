package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralSummaryProjectionFactory
import java.time.LocalDateTime
import java.util.UUID

class ReferralServiceTest {

  companion object {
    const val PRISON_NUMBER = "A1234AA"
  }

  @MockK(relaxed = true)
  private lateinit var referralRepository: ReferralRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: JpaOfferingRepository

  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    referralService = ReferralService(referralRepository, offeringRepository)
  }

  @Nested
  @DisplayName("Get Referral Summaries")
  inner class ReferralSummaryTests {
    @Test
    fun `getReferralsByOrganisationId with valid organisationId should return pageable ReferralSummary objects`() {
      val orgId = "MDI"
      val pageable = PageRequest.of(0, 10)

      val firstReferralId = UUID.randomUUID()
      val audiencesForFirstReferral = listOf("Audience 1", "Audience 2", "Audience 3")
      val projectionsForFirstReferral = audiencesForFirstReferral.map { audience ->
        ReferralSummaryProjectionFactory()
          .withReferralId(firstReferralId)
          .withCourseName("Course name")
          .withAudience(audience)
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
          .withPrisonNumber(PRISON_NUMBER)
          .produce()
      }

      val secondReferralId = UUID.randomUUID()
      val audiencesForSecondReferral = listOf("Audience 4", "Audience 5", "Audience 6")
      val projectionsForSecondReferral = audiencesForSecondReferral.map { audience ->
        ReferralSummaryProjectionFactory()
          .withReferralId(secondReferralId)
          .withCourseName("Another course name")
          .withAudience(audience)
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED)
          .withSubmittedOn(LocalDateTime.MIN)
          .withPrisonNumber(PRISON_NUMBER)
          .produce()
      }

      every { referralRepository.getReferralsByOrganisationId(orgId, pageable) } returns
        PageImpl(projectionsForFirstReferral + projectionsForSecondReferral, pageable, 2L)

      val resultPage = referralService.getReferralsByOrganisationId(orgId, pageable)

      resultPage.totalPages shouldBe 1
      resultPage.content shouldHaveSize 2
      resultPage.totalElements shouldBe 2

      val firstReferral = resultPage.content.find { it.id == firstReferralId }
      firstReferral shouldNotBe null
      firstReferral?.let { referral ->
        with(referral) {
          courseName shouldBe "Course name"
          audiences shouldBe listOf("Audience 1", "Audience 2", "Audience 3")
          status shouldBe ReferralStatus.referralStarted
          prisonNumber shouldBe PRISON_NUMBER
        }
      }

      val secondReferral = resultPage.content.find { it.id == secondReferralId }
      secondReferral shouldNotBe null
      secondReferral?.let { referral ->
        with(referral) {
          courseName shouldBe "Another course name"
          audiences shouldBe listOf("Audience 4", "Audience 5", "Audience 6")
          status shouldBe ReferralStatus.referralSubmitted
          submittedOn shouldBe LocalDateTime.MIN.toString()
          prisonNumber shouldBe PRISON_NUMBER
        }
      }

      verify { referralRepository.getReferralsByOrganisationId(orgId, pageable) }
    }

    @Test
    fun `getReferralsByOrganisationId with random organisationId should return pageable empty list`() {
      val orgId = UUID.randomUUID().toString()
      val pageable = PageRequest.of(0, 10)

      every { referralRepository.getReferralsByOrganisationId(orgId, pageable) } returns PageImpl(emptyList())

      val resultPage = referralService.getReferralsByOrganisationId(orgId, pageable)
      resultPage.content shouldBe emptyList()
      resultPage.totalElements shouldBe 0

      verify { referralRepository.getReferralsByOrganisationId(orgId, pageable) }
    }
  }
}
