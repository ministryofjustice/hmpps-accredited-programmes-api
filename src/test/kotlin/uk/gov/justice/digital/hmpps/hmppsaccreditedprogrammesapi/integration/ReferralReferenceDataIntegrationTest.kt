package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_HINT

private const val WITHDRAWN = "WITHDRAWN"
private const val CATEGORY_ADMIN = "W_ADMIN"
private const val REASON_DUPLICATE = "W_DUPLICATE"

private const val DESELECTED = "DESELECTED"
private const val PERSONAL = "D_PERSONAL"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralReferenceDataIntegrationTest : IntegrationTestBase() {

  val withdrawnStatusExpected = ReferralStatusRefData(
    code = WITHDRAWN,
    description = REFERRAL_WITHDRAWN_DESCRIPTION,
    colour = REFERRAL_WITHDRAWN_COLOUR,
    hintText = REFERRAL_WITHDRAWN_HINT,
    hasNotes = true,
    hasConfirmation = false,
    draft = false,
    closed = true,
    hold = false,
    release = false,
    notesOptional = false,
  )
  val categoryExpected =
    ReferralStatusCategory(code = CATEGORY_ADMIN, description = "Administrative error", referralStatusCode = WITHDRAWN)
  val reasonExpected = ReferralStatusReason(
    code = REASON_DUPLICATE,
    description = "Duplicate referral",
    referralCategoryCode = CATEGORY_ADMIN,
  )

  @Test
  fun `get all referral statuses`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatuses()
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val withdrawnStatus = response.firstOrNull { it.code == WITHDRAWN }
    withdrawnStatus!! shouldBeEqual withdrawnStatusExpected
  }

  @Test
  fun `get single referral status`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatus(WITHDRAWN)
    response.shouldNotBeNull()
    response shouldBeEqual withdrawnStatusExpected
  }

  @Test
  fun `get all referral status categories for a code`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusCategories(WITHDRAWN)
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val category = response.firstOrNull { it.code == CATEGORY_ADMIN }
    category!! shouldBeEqual categoryExpected
  }

  @Test
  fun `get single referral status category`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusCategory(CATEGORY_ADMIN)
    response.shouldNotBeNull()
    response shouldBeEqual categoryExpected
  }

  @Test
  fun `get all referral status reasons for a code`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasons(WITHDRAWN, CATEGORY_ADMIN)
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val category = response.firstOrNull { it.code == REASON_DUPLICATE }
    category!! shouldBeEqual reasonExpected
  }

  @Test
  fun `get all deselection referral status reasons for a deselection closed`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasonsDeselectedFlag(DESELECTED, PERSONAL, false)

    response.shouldNotBeNull()
    response.size.shouldBeEqual(4)
  }

  @Test
  fun `get all deselection referral status reasons for a deselection open`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasonsDeselectedFlag(DESELECTED, PERSONAL, true)

    response.shouldNotBeNull()
    response.size.shouldBeEqual(3)
  }

  @Test
  fun `get single referral status reason`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReason(REASON_DUPLICATE)
    response.shouldNotBeNull()
    response shouldBeEqual reasonExpected
  }

  @Test
  fun `get diagram`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getStatusTransitionDiagram()
    response.shouldNotBeNull()
  }

  fun getReferralStatuses() =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusRefData>>()
      .returnResult().responseBody!!

  fun getReferralStatus(code: String) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/$code")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<ReferralStatusRefData>()
      .returnResult().responseBody!!

  fun getStatusTransitionDiagram() =
    webTestClient
      .get()
      .uri("/status-transition-diagram")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

  fun getReferralStatusCategories(statusCode: String) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/$statusCode/categories")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusCategory>>()
      .returnResult().responseBody!!

  fun getReferralStatusCategory(code: String) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/categories/$code")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<ReferralStatusCategory>()
      .returnResult().responseBody!!

  fun getReferralStatusReasons(statusCode: String, categoryCode: String) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/$statusCode/categories/$categoryCode/reasons")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusReason>>()
      .returnResult().responseBody!!

  fun getReferralStatusReasonsDeselectedFlag(statusCode: String, categoryCode: String, deselectAndKeepOpen: Boolean) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/$statusCode/categories/$categoryCode/reasons?deselectAndKeepOpen=$deselectAndKeepOpen")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusReason>>()
      .returnResult().responseBody!!

  fun getReferralStatusReason(code: String) =
    webTestClient
      .get()
      .uri("/reference-data/referral-statuses/categories/reasons/$code")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<ReferralStatusReason>()
      .returnResult().responseBody!!
}
