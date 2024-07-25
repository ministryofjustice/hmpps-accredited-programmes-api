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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.CognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.NeedsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.PNIInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SexScores

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PniIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get pni info for prisoner successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val pniInfo = getPniInfoByPrisonNumber(prisonNumber)

    pniInfo shouldBe PNIInfo(
      needsScores = NeedsScores(
        sexScores = SexScores(
          sexualPreOccupation = null,
          offenceRelatedSexualInterests = null,
          emotionalCongruence = 0,
        ),
        cognitiveScores = CognitiveScores(
          proCriminalAttitudes = 1,
          hostileOrientation = null,
        ),
        relationshipScores = RelationshipScores(
          curRelCloseFamily = null,
          prevExpCloseRel = 2,
          easilyInfluenced = null,
          aggressiveControllingBehaviour = null,
        ),
        selfManagementScores = SelfManagementScores(
          impulsivity = null,
          temperControl = null,
          problemSolvingSkills = null,
          difficultiesCoping = null,
        ),
      ),
      riskScores = RiskScores(
        ogrs3 = "8.00".toBigDecimal(),
        ovp = "8.00".toBigDecimal(),
        ospDc = 1.07.toBigDecimal(),
        ospIic = 0.11.toBigDecimal(),
        rsr = 1.46.toBigDecimal(),
        sara = "High",
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
      .expectBody<PNIInfo>()
      .returnResult().responseBody!!
}
