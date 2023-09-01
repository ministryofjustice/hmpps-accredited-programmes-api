package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.CoursesControllerTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralService
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CoursesControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class ReferralsControllerTest
@Autowired
constructor(
  private val mockMvc: MockMvc,
  private val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var referralsService: ReferralService

  @Test
  fun `start a referral`() {
    val offeringId = UUID.randomUUID()
    val prisonNumber = "A1234BC"
    val referrerId = "XWK1234MN"
    val referralId = UUID.randomUUID()
    every { referralsService.startReferral(any(), any(), any()) } returns referralId

    mockMvc.post("/referrals") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      this.content = """{
        "offeringId": "$offeringId",
        "prisonNumber": "$prisonNumber",
        "referrerId": "$referrerId"
      }"""
    }.andExpect {
      status { isCreated() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        json("""{ "referralId": "$referralId" }""")
      }
    }

    verify { referralsService.startReferral(prisonNumber, offeringId, referrerId) }
  }

  @Test
  fun `get a referral`() {
    val offeringId = UUID.randomUUID()
    val prisonNumber = "A1234BC"
    val referrerId = "XWK1234MN"
    val referralId = UUID.randomUUID()

    every { referralsService.getReferral(any()) } returns Referral(id = referralId, offeringId = offeringId, prisonNumber = prisonNumber, referrerId = referrerId)

    mockMvc.get("/referrals/$referralId") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        json(
          """
          { 
            "offeringId": "$offeringId",
            "prisonNumber": "$prisonNumber",
            "referrerId": "$referrerId",
            "status": "referral_started",
            "oasysConfirmed": false,
            "reason": null
          }
          """,
        )
      }
    }

    verify { referralsService.getReferral(referralId) }
  }

  @Test
  fun `get a referral - not found`() {
    val referralId = UUID.randomUUID()

    every { referralsService.getReferral(any()) } returns null

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

    verify { referralsService.getReferral(referralId) }
  }

  @Test
  fun `successful referral status update`() {
    val referralId = UUID.randomUUID()

    every { referralsService.updateReferralStatus(any(), any()) } just Runs

    mockMvc.put("/referrals/$referralId/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = """{ "status": "referral_submitted" }"""
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralsService.updateReferralStatus(referralId, REFERRAL_SUBMITTED) }
  }

  @Test
  fun `referral status update - invalid transition`() {
    val referralId = UUID.randomUUID()

    every { referralsService.updateReferralStatus(any(), any()) } throws (IllegalArgumentException("Transition from $REFERRAL_STARTED to $AWAITING_ASSESSMENT is not valid"))

    mockMvc.put("/referrals/$referralId/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = """{ "status": "awaiting_assessment" }"""
    }.andExpect {
      status { isConflict() }
    }

    verify { referralsService.updateReferralStatus(referralId, AWAITING_ASSESSMENT) }
  }
}
