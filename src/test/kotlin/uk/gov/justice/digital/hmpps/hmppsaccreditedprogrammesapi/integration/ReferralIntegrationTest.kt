package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralIntegrationTest : IntegrationTestBase() {

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
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
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
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
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
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Retrieving a list of filtered referrals for an organisation should return 200 with correct body`() {
    val courseId = getFirstCourseId()
    val offeringId = getFirstOfferingIdForCourse(courseId)
    val referralCreated = createReferral(offeringId)
    val createdReferral = getReferralById(referralCreated.referralId)
    val organisationId = "MDI"

    referralCreated.referralId.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    val statusFilter = createdReferral.status.toDomain().name
    val audienceFilter = getCourseById(courseId).audiences.map { it.value }.first()
    val summary = getReferralSummariesByOrganisationId(organisationId, statusFilter, audienceFilter)

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralSummary(
          id = createdReferral.id,
          courseName = getCourseById(courseId).name,
          audiences = getCourseById(courseId).audiences.map { it.value }.sorted(),
          status = createdReferral.status,
          submittedOn = createdReferral.submittedOn,
          prisonNumber = createdReferral.prisonNumber,
        ),
      ).forEach { expectedSummary ->
        actualSummary.id shouldBe expectedSummary.id
        actualSummary.courseName shouldBe expectedSummary.courseName
        actualSummary.audiences shouldContainExactlyInAnyOrder expectedSummary.audiences
        actualSummary.status shouldBe expectedSummary.status
        actualSummary.submittedOn shouldBe expectedSummary.submittedOn
        actualSummary.prisonNumber shouldBe expectedSummary.prisonNumber
      }
    }
  }

  @Test
  fun `Retrieving a list of referrals for an organisation with no referrals should return 200 with empty body`() {
    val randomOrganisationId = randomUppercaseString(3)
    val paginatedReferralSummaries = getReferralSummariesByOrganisationId(randomOrganisationId)
    paginatedReferralSummaries.content?.shouldBeEmpty()
  }

  fun createReferral(offeringId: UUID) =
    webTestClient
      .post()
      .uri("/referrals")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralCreate(
          offeringId = offeringId,
          prisonNumber = PRISON_NUMBER,
          referrerId = REFERRER_ID,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<ReferralCreated>()
      .returnResult().responseBody!!

  fun getReferralById(createdReferralId: UUID) =
    webTestClient
      .get()
      .uri("/referrals/$createdReferralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Referral>()
      .returnResult().responseBody!!

  fun updateReferral(referralId: UUID, referralUpdate: ReferralUpdate): Any =
    webTestClient
      .put()
      .uri("/referrals/$referralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(referralUpdate)
      .exchange()
      .expectStatus().isNoContent

  fun submitReferral(createdReferralId: UUID) {
    webTestClient
      .post()
      .uri("/referrals/$createdReferralId/submit")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  fun getReferralSummariesByOrganisationId(
    organisationId: String,
    statusFilter: String? = null,
    audienceFilter: String? = null,
  ): PaginatedReferralSummary {
    val uriBuilder = UriComponentsBuilder.fromUriString("/referrals/organisation/$organisationId/dashboard")
    statusFilter?.let { uriBuilder.queryParam("status", it) }
    audienceFilter?.let { uriBuilder.queryParam("audience", it) }

    return webTestClient
      .get()
      .uri(uriBuilder.toUriString())
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PaginatedReferralSummary>()
      .returnResult().responseBody!!
  }
}
