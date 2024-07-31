package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.NeedsScore

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PniIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get pni info for prisoner successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val pniInfo = getPniInfoByPrisonNumber(prisonNumber)

    pniInfo shouldBe NeedsScore(
      overallNeedsScore = 6,
      domainScore = DomainScore(
        sexDomainScore = 2,
        thinkingDomainScore = 1,
        relationshipDomainScore = 1,
        selfManagementDomainScore = 2,
      ),
    )
  }

  fun getPniInfoByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/pni/$prisonNumber")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<NeedsScore>()
      .returnResult().responseBody!!
}
