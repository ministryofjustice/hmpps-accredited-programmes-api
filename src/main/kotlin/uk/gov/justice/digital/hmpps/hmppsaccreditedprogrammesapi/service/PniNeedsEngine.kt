package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualNeedsAndRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.areAllValuesPresent

@Service
class PniNeedsEngine {
  val validOspLevels = listOf("LOW", "MEDIUM", "HIGH", "VERY_HIGH")

  fun getOverallNeedsScore(
    individualNeedsAndRiskScores: IndividualNeedsAndRiskScores,
    prisonNumber: String,
    gender: String,
    basicSkillsScore: Int?,
  ): NeedsScore {
    val overallSexDomainScore = getSexDomainScore(individualNeedsAndRiskScores, prisonNumber, gender)
    val individualNeedsScores = individualNeedsAndRiskScores.individualNeedsScores
    val overallThinkingDomainScore = individualNeedsScores.individualCognitiveScores.overallCognitiveDomainScore()
    val overallRelationshipDomainScore = individualNeedsScores.individualRelationshipScores.overallRelationshipScore()
    val overallSelfManagementScore = individualNeedsScores.individualSelfManagementScores.overallSelfManagementScore()

    val overallNeedsScore = listOf(overallSexDomainScore, overallThinkingDomainScore, overallRelationshipDomainScore, overallSelfManagementScore).sum()

    return NeedsScore(
      overallNeedsScore = overallNeedsScore,
      classification = getClassification(overallNeedsScore),
      domainScore = DomainScore(
        sexDomainScore = SexDomainScore(
          overAllSexDomainScore = overallSexDomainScore,
          individualSexScores = individualNeedsScores.individualSexScores,
        ),
        thinkingDomainScore = ThinkingDomainScore(
          overallThinkingDomainScore = overallThinkingDomainScore,
          individualThinkingScores = individualNeedsScores.individualCognitiveScores,
        ),
        relationshipDomainScore = RelationshipDomainScore(
          overallRelationshipDomainScore = overallRelationshipDomainScore,
          individualRelationshipScores = individualNeedsScores.individualRelationshipScores,
        ),
        selfManagementDomainScore = SelfManagementDomainScore(
          overallSelfManagementDomainScore = overallSelfManagementScore,
          individualSelfManagementScores = individualNeedsScores.individualSelfManagementScores,
        ),
      ),
      basicSkillsScore = basicSkillsScore,
    )
  }

  fun getSexDomainScore(individualNeedsAndRiskScores: IndividualNeedsAndRiskScores, prisonNumber: String, gender: String?): Int {
    val individualSexScores = individualNeedsAndRiskScores.individualNeedsScores.individualSexScores

    if (individualSexScores.areAllValuesPresent()) {
      return individualSexScores.overallSexDomainScore(individualSexScores.totalScore())
    }

    val individualRiskScores = individualNeedsAndRiskScores.individualRiskScores
    if (gender.equals("Male", ignoreCase = true) &&
      (individualRiskScores.ospDc?.let { validOspLevels.contains(it) } == true || individualRiskScores.ospIic?.let { validOspLevels.contains(it) } == true)
    ) {
      throw BusinessException("PNI information cannot be computed for $gender prisoner $prisonNumber as ospDC or OspII scores are present but some values of SexDomainScore are null")
    }

    if (gender.equals("Female", ignoreCase = true) && individualSexScores.hasSomeDataPresent()) {
      throw BusinessException("PNI information cannot be computed for $gender prisoner $prisonNumber some SexDomainScore is present but not all")
    }
    return 0
  }
}

private fun getClassification(overallNeedsScore: Int?): String {
  return when (overallNeedsScore) {
    null -> NeedsClassification.INFORMATION_MISSING.name
    in 0..2 -> NeedsClassification.LOW_NEED.name
    in 3..5 -> NeedsClassification.MEDIUM_NEED.name
    in 6..8 -> NeedsClassification.HIGH_NEED.name
    else -> throw BusinessException("Unable to compute classification. Overall needs score is $overallNeedsScore")
  }
}

enum class NeedsClassification {
  LOW_NEED,
  MEDIUM_NEED,
  HIGH_NEED,
  INFORMATION_MISSING,
}
