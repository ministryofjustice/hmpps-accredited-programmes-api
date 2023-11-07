package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Person
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
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
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
      submittedOn = null,
    )
  }

  @Test
  fun `Updating a referral with a valid payload should return 204 with no body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferralId = createReferral(offeringId).referralId

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    updateReferral(createdReferralId, referralUpdate)

    getReferralById(createdReferralId) shouldBeEqual Referral(
      id = createdReferralId,
      offeringId = offeringId,
      referrerId = REFERRER_ID,
      prisonNumber = PRISON_NUMBER,
      status = ReferralStatus.referralStarted,
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      submittedOn = null,
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
          additionalInformation = "Additional information",
          oasysConfirmed = true,
          hasReviewedProgrammeHistory = true,
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
      additionalInformation = null,
      submittedOn = null,
    )
  }

  @Test
  fun `Submitting a referral with all fields set should return 204 with no body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferralId = createReferral(offeringId).referralId

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    updateReferral(createdReferralId, referralUpdate)
    val readyToSubmitReferral = getReferralById(createdReferralId)

    submitReferral(readyToSubmitReferral.id)

    getReferralById(readyToSubmitReferral.id).status shouldBeEqual ReferralStatus.referralSubmitted
  }

  @Test
  fun `Submitting a nonexistent referral should return 404 with error body`() {
    webTestClient
      .post()
      .uri("/referrals/${UUID.randomUUID()}/submit")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Retrieving a list of referrals for an organisation should return 200 with correct body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val createdReferral = createReferral(offeringId)
    val organisationId = "MDI"

    createdReferral.referralId.shouldNotBeNull()

    val paginatedReferralSummaries = getReferralSummariesByOrganisationId(organisationId)

    paginatedReferralSummaries.content?.shouldContainAnyOf(
      listOf(
        ReferralSummary(
          referralId = createdReferral.referralId,
          person = Person(prisonNumber = PRISON_NUMBER),
          referralStatus = ReferralStatus.referralStarted,
        ),
      ),
    )
  }

  @Test
  fun `Retrieving a list of referrals for an organisation with no referrals should return 200 with empty body`() {
    val randomOrganisationId = randomUppercaseString(3)
    val paginatedReferralSummaries = getReferralSummariesByOrganisationId(randomOrganisationId)
    paginatedReferralSummaries.content shouldBe emptyList()
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

  fun updateReferral(referralId: UUID, referralUpdate: ReferralUpdate): Any = webTestClient
    .put()
    .uri("/referrals/$referralId")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(referralUpdate)
    .exchange()
    .expectStatus().isNoContent

  private fun submitReferral(createdReferralId: UUID) {
    webTestClient
      .post()
      .uri("/referrals/$createdReferralId/submit")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  private fun getReferralSummariesByOrganisationId(organisationId: String): PaginatedReferralSummary = webTestClient
    .get()
    .uri("/referrals/organisation/$organisationId/dashboard")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<PaginatedReferralSummary>()
    .returnResult().responseBody!!
}
