package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StartReferral
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
  @DirtiesContext
  fun `create and retrieve a referral`() {
    val courseId: UUID = getCourses()!!.first().id
    val offeringId: UUID = getOfferings(courseId)!!.first().id

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

    val referral = webTestClient
      .get()
      .uri("/referrals/{referralId}", referralStarted.referralId)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody(ApiReferral::class.java)
      .returnResult().responseBody

    referral.shouldNotBeNull()

    referral shouldBeEqual ApiReferral(
      id = referralStarted.referralId,
      offeringId = offeringId,
      referrerId = "MWX0001",
      prisonNumber = "AB1234A",
      status = ReferralStatus.referralStarted,
    )
  }

  fun getCourses(): List<Course>? = webTestClient
    .get()
    .uri("/courses")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBodyList(Course::class.java)
    .returnResult().responseBody

  fun getOfferings(courseId: UUID): List<CourseOffering>? = webTestClient
    .get()
    .uri("courses/{courseId}/offerings", courseId)
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBodyList(CourseOffering::class.java)
    .returnResult()
    .responseBody
}
