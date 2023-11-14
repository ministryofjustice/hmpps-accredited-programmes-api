package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import jakarta.validation.ValidationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomReferrerId
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var referralService: ReferralService

  @Test
  fun `createReferral with JWT and valid payload returns 201 with correct body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrerId(randomReferrerId())
      .produce()

    val payload = mapOf(
      "offeringId" to referral.offering.id,
      "prisonNumber" to referral.prisonNumber,
      "referrerId" to referral.referrerId,
    )

    every { referralService.createReferral(any(), any(), any()) } returns referral.id

    mockMvc.post("/referrals") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isCreated() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.referralId") { value(referral.id.toString()) }
      }
    }

    verify { referralService.createReferral(referral.prisonNumber, referral.offering.id!!, referral.referrerId) }
  }

  @Test
  fun `getReferralById with JWT returns 200 with correct body`() {
    val referral = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .produce()

    every { referralService.getReferralById(any()) } returns referral

    mockMvc.get("/referrals/${referral.id}") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.offeringId") { value(referral.offering.id.toString()) }
        jsonPath("$.prisonNumber") { value(referral.prisonNumber) }
        jsonPath("$.referrerId") { value(referral.referrerId) }
        jsonPath("$.status") { REFERRAL_STARTED }
        jsonPath("$.oasysConfirmed") { value(true) }
        jsonPath("$.hasReviewedProgrammeHistory") { value(true) }
        jsonPath("$.additionalInformation") { doesNotExist() }
      }
    }

    verify { referralService.getReferralById(referral.id!!) }
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

    mockMvc.get("/referrals/$referralId") {
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
        jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `updateReferralStatusById with valid transition returns 204 with no body`() {
    val referral = ReferralEntityFactory().produce()

    val payload = mapOf(
      "status" to "referral_submitted",
    )

    every { referralService.updateReferralStatusById(any(), any()) } just Runs

    mockMvc.put("/referrals/${referral.id}/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralService.updateReferralStatusById(referral.id!!, REFERRAL_SUBMITTED) }
  }

  @Test
  fun `updateReferralStatusById with invalid transition returns 409 with no body`() {
    val referral = ReferralEntityFactory().produce()

    val payload = mapOf(
      "status" to "awaiting_assessment",
    )

    val illegalArgumentException =
      IllegalArgumentException("Transition from $REFERRAL_STARTED to $AWAITING_ASSESSMENT is not valid")

    every { referralService.updateReferralStatusById(any(), any()) } throws illegalArgumentException

    mockMvc.put("/referrals/${referral.id}/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isConflict() }
    }

    verify { referralService.updateReferralStatusById(referral.id!!, AWAITING_ASSESSMENT) }
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
      .withReferrerId(randomReferrerId())
      .withAdditionalInformation("Additional Info")
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.submitReferralById(referral.id!!) } just Runs

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
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
      .withReferrerId(randomReferrerId())
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.submitReferralById(referral.id!!) } throws ValidationException("additionalInformation is not valid: null")

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
  }

  @Test
  fun `getReferralsByOrganisationId with valid organisationId returns 200 with paginated body`() {
    val organisationId = "MDI"
    val pageable = PageRequest.of(0, 10)
    val referrals = listOf(
      ReferralEntityFactory()
        .withOfferingId(UUID.randomUUID())
        .withPrisonNumber(randomPrisonNumber())
        .withReferrerId(randomReferrerId())
        .withStatus(REFERRAL_STARTED)
        .produce(),
    )

    every { referralService.getReferralsByOrganisationId(organisationId, pageable) } returns PageImpl(referrals, pageable, referrals.size.toLong())

    mockMvc.get("/referrals/organisation/$organisationId/dashboard") {
      param("page", "0")
      param("size", "10")
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.content[0].referralId") { value(referrals[0].id.toString()) }
        jsonPath("$.content[0].person.prisonNumber") { value(referrals[0].prisonNumber) }
        jsonPath("$.content[0].referralStatus") { REFERRAL_STARTED }
        jsonPath("$.pageSize") { value(10) }
        jsonPath("$.totalElements") { value(1) }
        jsonPath("$.totalPages") { value(1) }
        jsonPath("$.pageNumber") { value(0) }
        jsonPath("$.pageIsEmpty") { value(false) }
      }
    }

    verify { referralService.getReferralsByOrganisationId(organisationId, pageable) }
  }

  @Test
  fun `getReferralsByOrganisationId without JWT returns 401`() {
    mockMvc.get("/referrals/organisation/${UUID.randomUUID()}/dashboard") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }

  @Test
  fun `getReferralsByOrganisationId with random organisationId returns 200 with paginated empty body`() {
    val orgId = UUID.randomUUID().toString()
    val pageable = PageRequest.of(0, 10)

    every { referralService.getReferralsByOrganisationId(orgId, pageable) } returns PageImpl(emptyList(), pageable, 0)

    mockMvc.get("/referrals/organisation/$orgId/dashboard") {
      param("page", "0")
      param("size", "10")
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.pageSize") { value(10) }
        jsonPath("$.totalElements") { value(0) }
        jsonPath("$.totalPages") { value(0) }
        jsonPath("$.pageNumber") { value(0) }
        jsonPath("$.pageIsEmpty") { value(true) }
      }
    }

    verify { referralService.getReferralsByOrganisationId(orgId, pageable) }
  }
}
