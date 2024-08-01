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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.RiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.ThinkingDomainScore
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PniIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get pni info for prisoner successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val pniScore = getPniInfoByPrisonNumber(prisonNumber)

    pniScore shouldBe PniScore(
      prisonNumber = prisonNumber,
      crn = "X739590",
      assessmentId = 2114584,
      needsScore = NeedsScore(
        overallNeedsScore = 6,
        domainScore = DomainScore(
          sexDomainScore = SexDomainScore(
            overAllSexDomainScore = 2,
            individualSexScores = IndividualSexScores(
              sexualPreOccupation = 2,
              offenceRelatedSexualInterests = 2,
              emotionalCongruence = 0,
            ),
          ),
          thinkingDomainScore = ThinkingDomainScore(
            overallThinkingDomainScore = 1,
            individualThinkingScores = IndividualCognitiveScores(
              proCriminalAttitudes = 1,
              hostileOrientation = 1,
            ),
          ),
          relationshipDomainScore = RelationshipDomainScore(
            overallRelationshipDomainScore = 1,
            individualRelationshipScores = IndividualRelationshipScores(
              curRelCloseFamily = 0,
              prevExpCloseRel = 2,
              easilyInfluenced = 1,
              aggressiveControllingBehaviour = 1,
            ),
          ),
          selfManagementDomainScore = SelfManagementDomainScore(
            overallSelfManagementDomainScore = 2,
            individualSelfManagementScores = IndividualSelfManagementScores(
              impulsivity = 1,
              temperControl = 4,
              problemSolvingSkills = 2,
              difficultiesCoping = null,
            ),
          ),
        ),
      ),
      riskScores = RiskScores(
        ogrs3 = "15.00".toBigDecimal(),
        ovp = "15.00".toBigDecimal(),
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
      .expectBody<PniScore>()
      .returnResult().responseBody!!
}
