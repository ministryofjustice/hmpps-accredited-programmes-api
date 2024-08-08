package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CaseLoad

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class UserIntegrationTest : IntegrationTestBase() {

  @Test
  fun `get caseloads by logged in user is successful when allCaseloads is set to false`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val caseLoads = getCurrentUserCaseload(false)
    caseLoads.shouldNotBeNull()
    caseLoads.first() shouldBeEqual CaseLoad(
      caseLoadId = "MDI",
      description = "Moorland Closed (HMP & YOI)",
      type = "INST",
      caseloadFunction = "GENERAL",
      currentlyActive = false,
    )
  }

  @Test
  fun `get caseloads by logged in user is successful when allCaseloads is set to true`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val caseLoads = getCurrentUserCaseload(true)
    caseLoads.shouldNotBeNull()
    caseLoads.first() shouldBeEqual CaseLoad(
      caseLoadId = "WTI",
      description = "HM Whatton",
      type = "INST",
      caseloadFunction = "GENERAL",
      currentlyActive = false,
    )
  }

  private fun getCurrentUserCaseload(caseLoad: Boolean): List<CaseLoad> =
    webTestClient
      .get()
      .uri("/user/me/caseload?allCaseloads=$caseLoad")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CaseLoad>>()
      .returnResult().responseBody!!
}
