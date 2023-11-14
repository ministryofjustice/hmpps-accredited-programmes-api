package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.UUID

private const val PRISON_NUMBER = "ABC123"

class ReferralServiceTest {

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
      val id = UUID.randomUUID()
      val pageable = PageRequest.of(0, 10)
      val referralEntities = listOf(
        ReferralEntityFactory()
          .withId(id)
          .withOfferingId(id)
          .withPrisonNumber(prisonNumber)
          .withReferrerId(id.toString())
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
          .produce(),
      )
      val pageOfReferrals = PageImpl(referralEntities, pageable, referralEntities.size.toLong())

      every { referralRepository.getReferralsByOrganisationId(orgId, pageable) } returns pageOfReferrals

      val referralSummariesPage = referralService.getReferralsByOrganisationId(orgId, pageable)
      referralSummariesPage.content shouldHaveSize referralEntities.size
      referralSummariesPage.totalElements shouldBe referralEntities.size.toLong()

      with(referralSummariesPage.content[0]) {
        prisonNumber shouldBe prisonNumber
        id shouldBe id
        offeringId shouldBe id
        referrerId shouldBe id.toString()
      }

      verify { referralRepository.getReferralsByOrganisationId(orgId, pageable) }
    }

    @Test
    fun `getReferralsByOrganisationId with random organisationid should return pageable empty list`() {
      val orgId = UUID.randomUUID().toString()
      val pageable = PageRequest.of(0, 10)

      every { referralRepository.getReferralsByOrganisationId(orgId, pageable) } returns PageImpl(emptyList())

      val referralSummariesPage = referralService.getReferralsByOrganisationId(orgId, pageable)
      referralSummariesPage.content shouldBe emptyList()
      referralSummariesPage.totalElements shouldBe 0

      verify { referralRepository.getReferralsByOrganisationId(orgId, pageable) }
    }
  }
}
