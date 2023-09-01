package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StartReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralsIntegrationTest
@Autowired
constructor(
  val webTestClient: WebTestClient,
  val jwtAuthHelper: JwtAuthHelper,
) {
  @Test
  fun `create and retrieve a referral`() {
    val courseId: UUID = getACourseId()
    val offeringId: UUID = getACourseOfferingId(courseId)

    val referralStarted = webTestClient
      .post()
      .uri("/referrals")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(StartReferral(offeringId = offeringId, referrerId = "MWX0001", prisonNumber = "AB1234A"))
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody(ReferralStarted::class.java)
      .returnResult().responseBody

    referralStarted.shouldNotBeNull()

    val referral = getReferral(referralStarted.referralId)

    referral shouldBeEqual ApiReferral(
      id = referralStarted.referralId,
      offeringId = offeringId,
      referrerId = "MWX0001",
      prisonNumber = "AB1234A",
      status = ReferralStatus.referralStarted,
      oasysConfirmed = false,
      reason = null,
    )
  }

  @Test
  fun `update a referral`() {
    val courseId: UUID = getACourseId()
    val offeringId: UUID = getACourseOfferingId(courseId)
    val referralId: UUID = startReferral(offeringId, "ReferrerId", "A1234AB")

    webTestClient
      .put()
      .uri("/referrals/{referralId}", referralId)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(ReferralUpdate(reason = "A Reason", oasysConfirmed = true))
      .exchange()
      .expectStatus().isNoContent

    val updatedReferral = getReferral(referralId)

    updatedReferral shouldBeEqual ApiReferral(
      id = referralId,
      offeringId = offeringId,
      referrerId = "ReferrerId",
      prisonNumber = "A1234AB",
      status = ReferralStatus.referralStarted,
      oasysConfirmed = true,
      reason = "A Reason",
    )
  }

  @Test
  fun `update a missing referral`() {
    webTestClient
      .put()
      .uri("/referrals/{referralId}", UUID.randomUUID())
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(ReferralUpdate(reason = "A Reason", oasysConfirmed = true))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `update referral status`() {
    val courseId: UUID = getACourseId()
    val offeringId: UUID = getACourseOfferingId(courseId)
    val referralId: UUID = startReferral(offeringId, "ReferrerId", "A1234AB")

    webTestClient
      .put()
      .uri("/referrals/{referralId}/status", referralId)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(StatusUpdate(ReferralStatus.referralSubmitted))
      .exchange()
      .expectStatus().isNoContent

    val updatedReferral = getReferral(referralId)

    updatedReferral shouldBeEqual ApiReferral(
      id = referralId,
      offeringId = offeringId,
      referrerId = "ReferrerId",
      prisonNumber = "A1234AB",
      status = ReferralStatus.referralSubmitted,
      oasysConfirmed = false,
      reason = null,
    )
  }

  private fun getACourseId(): UUID = getCourses()!!.first().id
  private fun getACourseOfferingId(courseId: UUID) = getOfferings(courseId)!!.first().id
  private fun getCourses(): List<Course>? = webTestClient
    .get()
    .uri("/courses")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBodyList(Course::class.java)
    .returnResult().responseBody

  private fun getOfferings(courseId: UUID): List<CourseOffering>? = webTestClient
    .get()
    .uri("courses/{courseId}/offerings", courseId)
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBodyList(CourseOffering::class.java)
    .returnResult()
    .responseBody

  private fun startReferral(offeringId: UUID, referrerId: String, prisonNumber: String): UUID = webTestClient
    .post()
    .uri("/referrals")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(StartReferral(offeringId = offeringId, referrerId = referrerId, prisonNumber = prisonNumber))
    .exchange()
    .expectStatus().is2xxSuccessful
    .expectBody(ReferralStarted::class.java)
    .returnResult().responseBody!!.referralId

  private fun getReferral(referralId: UUID): ApiReferral = webTestClient
    .get()
    .uri("/referrals/{referralId}", referralId)
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().is2xxSuccessful
    .expectBody(ApiReferral::class.java)
    .returnResult().responseBody!!
}
