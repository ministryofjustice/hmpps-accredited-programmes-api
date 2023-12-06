package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.*
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralSummaryProjectionFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory
import java.util.Optional
import java.util.UUID
import java.util.stream.Stream

class ReferralServiceTest {

  companion object {

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
        Arguments.of("REFERRAL_STARTED", null, "referralSummary1", projections1),
        Arguments.of(null, "Audience 2", null, projections1 + projections2),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 3", null, projections2 + projections3),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 4", "referralSummary3", projections3),
        Arguments.of(null, null, "Course", projections1 + projections2 + projections3),
        Arguments.of("AWAITING_ASSESSMENT", null, null, emptyList<ReferralSummaryProjection>()),
        Arguments.of(null, "Audience X", null, emptyList<ReferralSummaryProjection>()),
        Arguments.of(null, null, "Course for referralSummaryX", emptyList<ReferralSummaryProjection>()),
      )
    }
  }

  @MockK(relaxed = true)
  private lateinit var referralRepository: ReferralRepository

  @MockK(relaxed = true)
  private lateinit var referrerUserRepository: ReferrerUserRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: JpaOfferingRepository

  @InjectMockKs
  private lateinit var referralService: ReferralService

  @MockK(relaxed = true)
  private lateinit var prisonRegisterService: PrisonRegisterService

  @MockK(relaxed = true)
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  private val prisons = mapOf<String?, String>(ORGANISATION_ID to PRISON_NAME)
  private val prisoners = mapOf<String?, List<Prisoner>>(
    PRISON_NUMBER to listOf(
      Prisoner(
        prisonerNumber = PRISON_NUMBER,
        bookingId = BOOKING_ID,
        firstName = PRISONER_FIRST_NAME,
        lastName = PRISONER_LAST_NAME,
        nonDtoReleaseDateType = NONDTORELEASE_DATETYPE,
        indeterminateSentence = INDETERMINATE_SENTENCE,
      ),
    ),
  )
  val organisationId = "MDI"

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    every { prisonRegisterService.getAllPrisons() } returns prisons
    every { prisonerSearchApiService.getPrisoners(any()) } returns prisoners
  }

  private fun mockSecurityContext(username: String) {
    val authentication = mockk<Authentication>()
    every { authentication.name } returns username

    val securityContext = mockk<SecurityContext>()
    every { securityContext.authentication } returns authentication

    mockkStatic(SecurityContextHolder::class)
    every { SecurityContextHolder.getContext() } returns securityContext
  }

  @Test
  fun `createReferral with existing user should create referral successfully`() {
    mockSecurityContext(CLIENT_USERNAME)

    val referrer = ReferrerUserEntityFactory()
      .withUsername(CLIENT_USERNAME)
      .produce()
    every { referrerUserRepository.findById(CLIENT_USERNAME) } returns Optional.of(referrer)

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val createdReferralId = referralService.createReferral(PRISON_NUMBER, offering.id!!, REFERRER_ID)

    createdReferralId shouldBe referralId

    verify { referrerUserRepository.findById(CLIENT_USERNAME) }
    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER &&
            it.referrerId == REFERRER_ID &&
            it.referrer.username == CLIENT_USERNAME &&
            it.offering.id == offering.id
        },
      )
    }
  }

  @Test
  fun `createReferral with nonexistent user should create user and referral successfully`() {
    mockSecurityContext("NONEXISTENT_USER")

    every { referrerUserRepository.findById("NONEXISTENT_USER") } returns Optional.empty()

    every { referrerUserRepository.save(any<ReferrerUserEntity>()) } answers {
      firstArg<ReferrerUserEntity>().apply { username = "NONEXISTENT_USER" }
    }

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val createdReferralId = referralService.createReferral(PRISON_NUMBER, offering.id!!, REFERRER_ID)

    createdReferralId shouldBe referralId

    verify {
      referrerUserRepository.save(
        match {
          it.username == "NONEXISTENT_USER"
        },
      )
    }

    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER &&
            it.referrerId == REFERRER_ID &&
            it.referrer.username == "NONEXISTENT_USER" &&
            it.offering.id == offering.id
        },
      )
    }
  }

  @ParameterizedTest
  @MethodSource("parametersForGetReferralsByOrganisationId")
  fun `getReferralsByOrganisationId with valid organisationId and filtering should return pageable ReferralSummary objects`(
    statusFilter: String?,
    audienceFilter: String?,
    courseFilter: String?,
    expectedReferralSummaryProjections: List<ReferralSummaryProjection>,
  ) {
    val pageable = PageRequest.of(0, 10)
    val status = statusFilter?.let { ReferralEntity.ReferralStatus.valueOf(it) }

    every { referralRepository.getReferralsByOrganisationId(organisationId, pageable, status, audienceFilter, courseFilter) } returns
      PageImpl(expectedReferralSummaryProjections, pageable, expectedReferralSummaryProjections.size.toLong())

    val resultPage = referralService.getReferralsByOrganisationId(organisationId, pageable, statusFilter, audienceFilter, courseFilter)

    resultPage.totalElements shouldBe expectedReferralSummaryProjections.size.toLong()

    val expectedTotalPages = (expectedReferralSummaryProjections.size + pageable.pageSize - 1) / pageable.pageSize
    resultPage.totalPages shouldBe expectedTotalPages

    val expectedApiReferralSummaries = expectedReferralSummaryProjections.toApi(prisoners, prisons, organisationId)

    resultPage.totalElements shouldBe expectedApiReferralSummaries.size.toLong()
//    resultPage.content shouldContain expectedApiReferralSummaries

    verify { referralRepository.getReferralsByOrganisationId(organisationId, pageable, status, audienceFilter, courseFilter) }
  }

  @Test
  fun `getReferralsByOrganisationId with random organisationId should return pageable empty list`() {
    val orgId = UUID.randomUUID().toString()
    val pageable = PageRequest.of(0, 10)

    every { referralRepository.getReferralsByOrganisationId(orgId, pageable, null, null, null) } returns PageImpl(emptyList())

    val resultPage = referralService.getReferralsByOrganisationId(orgId, pageable, null, null, null)
    resultPage.content shouldBe emptyList()
    resultPage.totalElements shouldBe 0

    verify { referralRepository.getReferralsByOrganisationId(orgId, pageable, null, null, null) }
  }
}
