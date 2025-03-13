package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PniServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var pniService: PniService

  @Autowired
  lateinit var pniResultEntityRepository: PNIResultEntityRepository

  @Test
  fun `should throw NotFoundException when attempting to calculate PNI score for an unknown prisoner number`() {
    // Given
    val prisonNumber = "A99999"

    // When & Then
    val exception = assertThrows(NotFoundException::class.java) {
      pniService.getOasysPniScore(prisonNumber)
    }
    assertThat(exception.message).isEqualTo("No PNI information found for A99999")
  }

  @Test
  fun `should calculate PNI score for known prisoner number`() {
    // Given
    val prisonNumber = "A1234AA"

    // When
    val pniScore = pniService.getOasysPniScore(prisonNumber)

    // Then
    assertThat(pniScore).isNotNull
    assertThat(pniScore.prisonNumber).isEqualTo(prisonNumber)
    assertThat(pniScore.crn).isEqualTo("D006518")
    assertThat(pniScore.assessmentId).isEqualTo(10082385)
    assertThat(pniScore.programmePathway).isEqualTo("High Intensity")
    assertThat(pniScore.needsScore).isNotNull
    assertThat(pniScore.needsScore.overallNeedsScore).isEqualTo(5)
    assertThat(pniScore.needsScore.basicSkillsScore).isEqualTo(33)
    assertThat(pniScore.needsScore.classification).isEqualTo("High")
    assertThat(pniScore.needsScore.domainScore).isEqualTo(
      DomainScore(
        SexDomainScore(
          overAllSexDomainScore = 10,
          individualSexScores = IndividualSexScores(
            sexualPreOccupation = 2,
            offenceRelatedSexualInterests = 1,
            emotionalCongruence = 1,
          ),
        ),
        thinkingDomainScore = ThinkingDomainScore(
          overallThinkingDomainScore = 10,
          individualThinkingScores = IndividualCognitiveScores(
            proCriminalAttitudes = 1,
            hostileOrientation = 1,
          ),
        ),
        relationshipDomainScore = RelationshipDomainScore(
          overallRelationshipDomainScore = 10,
          individualRelationshipScores = IndividualRelationshipScores(
            curRelCloseFamily = 0,
            prevExpCloseRel = 1,
            easilyInfluenced = 0,
            aggressiveControllingBehaviour = 0,
          ),
        ),
        selfManagementDomainScore = SelfManagementDomainScore(
          overallSelfManagementDomainScore = 10,
          individualSelfManagementScores = IndividualSelfManagementScores(
            impulsivity = 0,
            temperControl = 2,
            problemSolvingSkills = 0,
            difficultiesCoping = 0,
          ),
        ),
      ),
    )
    assertThat(pniScore.riskScore).isNotNull
    assertThat(pniScore.riskScore.classification).isEqualTo("High")
    assertThat(pniScore.riskScore.individualRiskScores).isEqualTo(
      IndividualRiskScores(
        ogrs3 = null,
        ogrs3Risk = "High",
        ovpRisk = "Medium",
        ovp = null,
        ospDc = "Low",
        ospIic = "Low",
        rsr = BigDecimal("3.5"),
        sara = Sara(
          overallResult = SaraRisk.MEDIUM,
          saraRiskOfViolenceTowardsPartner = SaraRisk.NOT_APPLICABLE.name,
          saraRiskOfViolenceTowardsOthers = SaraRisk.MEDIUM.name,
          saraAssessmentId = 10082385,
        ),
      ),
    )
    assertThat(pniScore.validationErrors).isEmpty()
  }

  @Test
  fun `should persist PNI Score to the database`() {
    // Given
    val prisonNumber = "A1234AA"
    val pniScore = pniService.getOasysPniScore(prisonNumber)
    val courseId: UUID = UUID.fromString("790a2dfe-8df1-4504-bb9c-83e6e53a6537")
    val offeringId = "7fffcc6a-11f8-4713-be35-cf5ff1aee517"

    persistenceHelper.clearAllTableContent()
    persistenceHelper.createCourse(
      courseId,
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createPrerequisite(courseId, "pr name1", "pr description1")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")
    persistenceHelper.createOffering(
      UUID.fromString(offeringId),
      courseId,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    val referralId = UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa")
    persistenceHelper.createReferral(
      referralId,
      UUID.fromString(offeringId),
      "B2345BB",
      "TEST_REFERRER_USER_1",
      "This referral will be updated",
      false,
      false,
      "REFERRAL_STARTED",
      null,
    )

    // When
    pniService.savePni(pniScore, referralId)

    // Then
    val pniResults = pniResultEntityRepository.findAllByPrisonNumber(prisonNumber)
    assertThat(pniResults).hasSize(1)
    assertThat(pniResults[0].prisonNumber).isEqualTo(prisonNumber)
    assertThat(pniResults[0].crn).isEqualTo("D006518")
    assertThat(pniResults[0].referralId).isEqualTo(referralId)
    assertThat(pniResults[0].programmePathway).isEqualTo("High Intensity")
    assertThat(pniResults[0].needsClassification).isEqualTo("High")
    assertThat(pniResults[0].riskClassification).isEqualTo("High")
    assertThat(pniResults[0].basicSkillsScore).isEqualTo(33)
  }
}
