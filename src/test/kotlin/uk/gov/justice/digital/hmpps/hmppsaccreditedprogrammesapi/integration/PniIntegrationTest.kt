package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PniIntegrationTest :
  IntegrationTestBase() {

  @Autowired
  lateinit var pniResultEntityRepository: PNIResultEntityRepository

  @Test
  fun `Get pni info for prisoner successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val pniScore = getPniInfoByPrisonNumber(prisonNumber)

    pniScore shouldBe pniScore(prisonNumber)
  }

  fun pniScore(prisonNumber: String) = PniScore(
    prisonNumber = prisonNumber,
    crn = "X739590",
    assessmentId = 2114584,
    programmePathway = "MISSING_INFORMATION",
    needsScore = NeedsScore(
      overallNeedsScore = 6,
      basicSkillsScore = 33,
      classification = "HIGH_NEED",
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
    validationErrors = listOf("difficultiesCoping in SelfManagementScores is null"),
    riskScore = RiskScore(
      classification = "HIGH_RISK",
      individualRiskScores = IndividualRiskScores(
        ogrs3 = "15.00".toBigDecimal(),
        ovp = "15.00".toBigDecimal(),
        ospDc = 1.07.toBigDecimal(),
        ospIic = 0.11.toBigDecimal(),
        rsr = 1.46.toBigDecimal(),
        sara = "High",
      ),
    ),
  )

  @Test
  fun `Save pni info for prisoner successful`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    // When
    getPniInfoByPrisonNumberAndSave(prisonNumber)
    // Then
    val pniResults = pniResultEntityRepository.findAllByPrisonNumber(prisonNumber)
    pniResults[0].prisonNumber shouldBe prisonNumber
    pniResults[0].crn shouldBe pniScore(prisonNumber).crn
    pniResults[0].needsClassification shouldBe pniScore(prisonNumber).needsScore.classification
    pniResults[0].overallNeedsScore shouldBe pniScore(prisonNumber).needsScore.overallNeedsScore
    pniResults[0].programmePathway shouldBe pniScore(prisonNumber).programmePathway
    pniResults[0].riskClassification shouldBe pniScore(prisonNumber).riskScore.classification
    pniResults[0].pniResultJson shouldBe objectMapper.writeValueAsString(pniScore(prisonNumber))
    pniResults[0].pniValid shouldBe false
    pniResults[0].basicSkillsScore shouldBe 33
  }

  fun getPniInfoByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/PNI/$prisonNumber?gender=Male")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PniScore>()
      .returnResult().responseBody!!

  fun getPniInfoByPrisonNumberAndSave(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/PNI/$prisonNumber?gender=Male&savePNI=true")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PniScore>()
      .returnResult().responseBody!!
}
