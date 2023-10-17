package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralIntegrationTest : IntegrationTestBase() {
  companion object {
    const val PRISON_NUMBER = "A1234AA"
    const val REFERRER_ID = "MWX0001"
  }

  @Test
  fun `Creating a referral should return 201 with correct body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferralId = createReferral(offeringId).referralId

    createdReferralId.shouldNotBeNull()

    getReferralById(createdReferralId) shouldBeEqual Referral(
      id = createdReferralId,
      offeringId = offeringId,
      referrerId = REFERRER_ID,
      prisonNumber = PRISON_NUMBER,
      status = ReferralStatus.referralStarted,
      reason = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
    )
  }

  @Test
  fun `Updating a referral with a valid payload should return 204 with no body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferralId = createReferral(offeringId).referralId

    webTestClient
      .put()
      .uri("/referrals/$createdReferralId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralUpdate(
          reason = "A Reason",
          oasysConfirmed = true,
          hasReviewedProgrammeHistory = true,
        ),
      )
      .exchange()
      .expectStatus().isNoContent

    getReferralById(createdReferralId) shouldBeEqual Referral(
      id = createdReferralId,
      offeringId = offeringId,
      referrerId = REFERRER_ID,
      prisonNumber = PRISON_NUMBER,
      status = ReferralStatus.referralStarted,
      reason = "A Reason",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )
  }

  @Test
  fun `Updating a nonexistent referral should return 404 with error body`() {
    webTestClient
      .put()
      .uri("/referrals/${UUID.randomUUID()}")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralUpdate(
          reason = "A Reason",
          oasysConfirmed = true,
        ),
      )
      .exchange().expectStatus().isNotFound
  }

  @Test
  fun `Updating a referral status should return 204 with no body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferralId = createReferral(offeringId).referralId

    webTestClient
      .put()
      .uri("/referrals/$createdReferralId/status")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralStatusUpdate(
          status = ReferralStatus.referralSubmitted,
        ),
      )
      .exchange().expectStatus().isNoContent

    getReferralById(createdReferralId) shouldBeEqual Referral(
      id = createdReferralId,
      offeringId = offeringId,
      referrerId = REFERRER_ID,
      prisonNumber = PRISON_NUMBER,
      status = ReferralStatus.referralSubmitted,
      oasysConfirmed = false,
      reason = null,
    )
  }

  fun createReferral(offeringId: UUID) = webTestClient
    .post()
    .uri("/referrals")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(
      ReferralCreate(
        offeringId = offeringId,
        referrerId = REFERRER_ID,
        prisonNumber = PRISON_NUMBER,
      ),
    )
    .exchange()
    .expectStatus().isCreated
    .expectBody<ReferralCreated>()
    .returnResult().responseBody!!

  fun getReferralById(createdReferralId: UUID) = webTestClient
    .get()
    .uri("/referrals/$createdReferralId")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<Referral>()
    .returnResult().responseBody!!
}
