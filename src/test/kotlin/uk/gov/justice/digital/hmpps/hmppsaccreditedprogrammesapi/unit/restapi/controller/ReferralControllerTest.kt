package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReferralController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralStatusHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SecurityService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StaffService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralStatusRefDataFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.StaffEntityFactory
import java.util.UUID

private const val MY_REFERRALS_ENDPOINT = "/referrals/me/dashboard"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Import(JwtAuthHelper::class)
class ReferralControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var referralService: ReferralService

  @MockkBean
  private lateinit var securityService: SecurityService

  @MockkBean
  private lateinit var referralReferenceDataService: ReferralReferenceDataService

  @MockkBean
  private lateinit var referralStatusHistoryService: ReferralStatusHistoryService

  @MockkBean
  private lateinit var auditService: AuditService

  @MockkBean
  private lateinit var staffService: StaffService

  @MockkBean
  private lateinit var courseParticipationService: CourseParticipationService

  @Test
  fun `createReferral with JWT, existing user, and valid payload returns 201 with correct body`() {
    val referral: Referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrer(
        ReferrerUserEntityFactory()
          .withUsername(REFERRER_USERNAME)
          .produce(),
      )
      .produce().toApi()

    val payload = mapOf(
      "offeringId" to referral.offeringId,
      "prisonNumber" to referral.prisonNumber,
    )

    every { referralService.getDuplicateReferrals(referral.offeringId, referral.prisonNumber) } returns null
    every { referralService.createReferral(referral.prisonNumber, referral.offeringId) } returns referral

    mockMvc.post("/referrals") {
      contentType = MediaType.APPLICATION_JSON
      headers { set(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken()) }
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isCreated() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$..id") { value(referral.id.toString()) }
      }
    }

    verify { referralService.getDuplicateReferrals(referral.offeringId, referral.prisonNumber) }
    verify { referralService.createReferral(referral.prisonNumber, referral.offeringId) }
  }

  @Test
  @WithMockUser(username = "NONEXISTENT_USER")
  fun `createReferral with JWT, nonexistent user, and valid payload returns 201 with correct body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrer(
        ReferrerUserEntityFactory()
          .withUsername(SecurityContextHolder.getContext().authentication?.name.toString())
          .produce(),
      )
      .produce()

    SecurityContextHolder.getContext().authentication?.name shouldBe "NONEXISTENT_USER"

    val payload = mapOf(
      "offeringId" to referral.offering.id,
      "prisonNumber" to referral.prisonNumber,
    )

    every { referralService.getDuplicateReferrals(any(), any()) } returns null
    every { referralService.createReferral(any(), any()) } returns referral.toApi()

    mockMvc.post("/referrals") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isCreated() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$..id") { value(referral.id.toString()) }
      }
    }

    verify { referralService.createReferral(referral.prisonNumber, referral.offering.id!!) }
    verify { referralService.getDuplicateReferrals(referral.offering.id!!, referral.prisonNumber) }
    verify { referralService.createReferral(referral.prisonNumber, referral.offering.id!!) }
  }

  @Test
  fun `getReferralById with JWT returns 200 with correct body`() {
    val offeringEntity = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .produce()
    offeringEntity.course = CourseEntityFactory().produce()

    val referral = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withOffering(offeringEntity)
      .withReferrer(
        ReferrerUserEntityFactory()
          .withUsername(REFERRER_USERNAME)
          .produce(),
      )
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .produce()

    every { referralService.getReferralById(any()) } returns referral
    val referralStatus = ReferralStatusRefDataFactory()
      .withCode(REFERRAL_STARTED)
      .produce()
    every { referralReferenceDataService.getReferralStatus(REFERRAL_STARTED) } returns referralStatus
    every { staffService.getStaffDetail(any()) } returns StaffEntityFactory().produce()
    every { auditService.audit(any(), any(), org.mockito.kotlin.eq(AuditAction.VIEW_REFERRAL.name)) } returns Unit

    mockMvc.get("/referrals/${referral.id}?updatePerson=false") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.offeringId") { value(referral.offering.id.toString()) }
        jsonPath("$.prisonNumber") { value(referral.prisonNumber) }
        jsonPath("$.referrerUsername") { value(referral.referrer.username) }
        jsonPath("$.status") { REFERRAL_STARTED }
        jsonPath("$.oasysConfirmed") { value(true) }
        jsonPath("$.hasReviewedProgrammeHistory") { value(true) }
        jsonPath("$.additionalInformation") { doesNotExist() }
      }
    }

    verify { referralService.getReferralById(any()) }
    verify { auditService.audit(any(), any(), org.mockito.kotlin.eq(AuditAction.VIEW_REFERRAL.name)) }
  }

  @Test
  fun `getReferralById without JWT returns 401`() {
    mockMvc.get("/referrals/${UUID.randomUUID()}") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }

  @Test
  fun `getReferralById with random UUID returns 404 with error body`() {
    val referralId = UUID.randomUUID()

    every { referralService.getReferralById(any()) } returns null

    mockMvc.get("/referrals/$referralId?updatePerson=false") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNotFound() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(404) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Not Found: No Referral found at /referrals/$referralId") }
        jsonPath("$.developerMessage") { prefix("No referral found at /courses/$referralId") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }

    verify { referralService.getReferralById(referralId) }
  }

  @Test
  fun `getReferralById with invalid UUID returns 400 with error body`() {
    val referralId = "bad-id"

    mockMvc.get("/referrals/$referralId") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(400) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `updateReferralStatusById with valid transition returns 204 with no body`() {
    val referral = ReferralEntityFactory().produce()

    val payload = mapOf(
      "status" to "REFERRAL_SUBMITTED",
    )

    every { referralService.updateReferralStatusById(any(), any()) } just Runs

    mockMvc.put("/referrals/${referral.id}/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralService.updateReferralStatusById(referral.id!!, ReferralStatusUpdate(status = REFERRAL_SUBMITTED)) }
  }

  @Test
  fun `submitReferralById with valid ID returns 204 with no body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrer(
        ReferrerUserEntityFactory()
          .withUsername(REFERRER_USERNAME)
          .produce(),
      )
      .withAdditionalInformation("Additional Info")
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .withOverrideReason("Override Reason")
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.getDuplicateReferrals(referral.offering.id!!, referral.prisonNumber) } returns null
    every { referralService.submitReferralById(referral.id!!) } returns referral

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
    verify { referralService.getDuplicateReferrals(referral.offering.id!!, referral.prisonNumber) }
  }

  @Test
  fun `submitReferralById with invalid ID returns 404 with error body`() {
    val invalidReferralId = UUID.randomUUID()

    every { referralService.getReferralById(invalidReferralId) } returns null

    mockMvc.post("/referrals/$invalidReferralId/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNotFound() }
    }

    verify { referralService.getReferralById(invalidReferralId) }
  }

  @Test
  fun `submitReferralById with incomplete referral returns 400 with error body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrer(
        ReferrerUserEntityFactory()
          .withUsername(REFERRER_USERNAME)
          .produce(),
      )
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.getDuplicateReferrals(any(), any()) } returns null
    every { referralService.submitReferralById(referral.id!!) } throws ValidationException("additionalInformation is not valid: null")

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
  }

  @ParameterizedTest
  @ValueSource(strings = ["Del Hatton", "Del,Hatton", "Del, Hatton"])
  fun `search by name with comma works as expected`(nameSearch: String) {
    val referralService: ReferralService = mockk()
    val securityService: SecurityService = mockk()
    val referenceDataService: ReferralReferenceDataService = mockk()
    val referralStatusHistoryService: ReferralStatusHistoryService = mockk()
    val auditService: AuditService = mockk()
    val staffService: StaffService = mockk()
    val courseParticipationService: CourseParticipationService = mockk()

    val referralController = ReferralController(referralService, securityService, referenceDataService, referralStatusHistoryService, auditService, staffService, courseParticipationService)

    val parseNameOrId = referralController.parseNameOrId(nameSearch)
    parseNameOrId.forename shouldBe "DEL"
    parseNameOrId.surname shouldBe "HATTON"
    parseNameOrId.surnameOnly shouldBe ""
  }

  @Test
  fun `deleteReferralById removes referral and associated course participation records`() {
    // Given
    every { auditService.audit(any(), any(), org.mockito.kotlin.eq(AuditAction.DELETE_REFERRAL.name)) } returns Unit

    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory()
      .withId(referralId)
      .withOffering(OfferingEntityFactory().withId(UUID.randomUUID()).produce())
      .withPrisonNumber(randomPrisonNumber())
      .withReferrer(ReferrerUserEntityFactory().withUsername(REFERRER_USERNAME).produce())
      .produce()

    val draftReferralStatus = ReferralStatusRefDataFactory().withDraft(true).produce()
    every { referralReferenceDataService.getReferralStatus(any()) } returns draftReferralStatus
    every { referralService.getReferralById(referralId) } returns referral
    every { courseParticipationService.deleteAllCourseParticipationsForReferral(referralId) } just Runs
    every { referralService.deleteReferral(referralId) } just Runs

    // When
    mockMvc.delete("/referrals/$referralId") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNoContent() }
    }

    // Then
    verify { courseParticipationService.deleteAllCourseParticipationsForReferral(referralId) }
    verify { referralService.deleteReferral(referralId) }
  }
}
