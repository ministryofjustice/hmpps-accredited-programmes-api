package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.UUID

private const val prisonNumber = "ABC123"

class ReferralServiceTest {

  @MockK(relaxed = true)
  private lateinit var referralRepository: JpaReferralRepository

  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    referralService = ReferralService(referralRepository)
  }

  @Nested
  @DisplayName("Get referral summary for an orgId")
  inner class ReferralSummaryTest {
    @Test
    fun `list of referralEntities returned for an orgId`() {
      val orgId = "MDI"
      val id = UUID.randomUUID()
      val referralEntities = listOf(
        ReferralEntityFactory()
          .withId(id)
          .withOfferingId(id)
          .withPrisonNumber(prisonNumber)
          .withReferrerId(id.toString())
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
          .produce(),
      )

      every { referralRepository.getReferralsByOrgId(orgId) } returns referralEntities

      val referralSummaries = referralService.getReferralSummaryByOrgId(orgId)
      referralSummaries.shouldNotBeNull()
      referralSummaries.size.shouldBe(referralEntities.size)
      referralSummaries[0].prisonNumber.shouldBe(prisonNumber)
      referralSummaries[0].id.shouldBe(id)
      referralSummaries[0].offeringId.shouldBe(id)
      referralSummaries[0].referrerId.shouldBe(id.toString())
    }
  }
}