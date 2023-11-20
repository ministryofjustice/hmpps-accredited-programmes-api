package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.read.ReferralSummaryProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralSummaryProjectionFactory
import java.util.UUID
import java.util.stream.Stream

class ReferralServiceTest {

  companion object {
    private const val PRISON_NUMBER = "A1234AA"

    @JvmStatic
    fun parametersForGetReferralsByOrganisationId(): Stream<Arguments> {
      val projections1 = listOf("Audience 1", "Audience 2").map { audience ->
        ReferralSummaryProjectionFactory()
          .withReferralId(UUID.randomUUID())
          .withCourseName("Course for referralSummary1")
          .withAudience(audience)
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
          .withPrisonNumber(PRISON_NUMBER)
          .produce()
      }

      val projections2 = listOf("Audience 2", "Audience 3").map { audience ->
        ReferralSummaryProjectionFactory()
          .withReferralId(UUID.randomUUID())
          .withCourseName("Course for referralSummary2")
          .withAudience(audience)
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED)
          .withPrisonNumber(PRISON_NUMBER)
          .produce()
      }

      val projections3 = listOf("Audience 3", "Audience 4").map { audience ->
        ReferralSummaryProjectionFactory()
          .withReferralId(UUID.randomUUID())
          .withCourseName("Course for referralSummary3")
          .withAudience(audience)
          .withStatus(ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED)
          .withPrisonNumber(PRISON_NUMBER)
          .produce()
      }

      return Stream.of(
        Arguments.of("REFERRAL_STARTED", null, projections1),
        Arguments.of(null, "Audience 2", projections1 + projections2),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 3", projections2 + projections3),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 4", projections3),
        Arguments.of(null, null, projections1 + projections2 + projections3),
        Arguments.of("AWAITING_ASSESSMENT", null, emptyList<ReferralSummaryProjection>()),
        Arguments.of(null, "Audience X", emptyList<ReferralSummaryProjection>()),
      )
    }
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

  @ParameterizedTest
  @MethodSource("parametersForGetReferralsByOrganisationId")
  fun `getReferralsByOrganisationId with valid organisationId and filtering should return pageable ReferralSummary objects`(
    statusFilter: String?,
    audienceFilter: String?,
    expectedReferralSummaryProjections: List<ReferralSummaryProjection>,
  ) {
    val orgId = "MDI"
    val pageable = PageRequest.of(0, 10)
    val status = statusFilter?.let { ReferralEntity.ReferralStatus.valueOf(it) }

    every { referralRepository.getReferralsByOrganisationId(orgId, pageable, status, audienceFilter) } returns
      PageImpl(expectedReferralSummaryProjections, pageable, expectedReferralSummaryProjections.size.toLong())

    val resultPage = referralService.getReferralsByOrganisationId(orgId, pageable, statusFilter, audienceFilter)

    resultPage.totalElements shouldBe expectedReferralSummaryProjections.size.toLong()

    val expectedTotalPages = (expectedReferralSummaryProjections.size + pageable.pageSize - 1) / pageable.pageSize
    resultPage.totalPages shouldBe expectedTotalPages

    val expectedApiReferralSummaries = expectedReferralSummaryProjections.toApi()
    resultPage.content shouldContainAll expectedApiReferralSummaries

    verify { referralRepository.getReferralsByOrganisationId(orgId, pageable, status, audienceFilter) }
  }

  @Test
  fun `getReferralsByOrganisationId with random organisationId should return pageable empty list`() {
    val orgId = UUID.randomUUID().toString()
    val pageable = PageRequest.of(0, 10)

    every { referralRepository.getReferralsByOrganisationId(orgId, pageable, null, null) } returns PageImpl(emptyList())

    val resultPage = referralService.getReferralsByOrganisationId(orgId, pageable, null, null)
    resultPage.content shouldBe emptyList()
    resultPage.totalElements shouldBe 0

    verify { referralRepository.getReferralsByOrganisationId(orgId, pageable, null, null) }
  }
}
