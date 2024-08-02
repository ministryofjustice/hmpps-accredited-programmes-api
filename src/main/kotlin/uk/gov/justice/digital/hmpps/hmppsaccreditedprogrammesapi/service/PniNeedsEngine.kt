package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualNeedsAndRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.ThinkingDomainScore
import java.math.BigDecimal

private const val ZERO = 0

@Service
class PniNeedsEngine {

  fun getOverallNeedsScore(individualNeedsAndRiskScores: IndividualNeedsAndRiskScores, prisonNumber: String): NeedsScore {
    val sexDomainScore = getSexDomainScore(individualNeedsAndRiskScores, prisonNumber)
    val thinkingDomainScore = individualNeedsAndRiskScores.individualNeedsScores.individualCognitiveScores.overallCognitiveDomainScore()
    val relationshipDomainScore = individualNeedsAndRiskScores.individualNeedsScores.individualRelationshipScores.overallRelationshipScore()
    val selfManagementDomainScore = individualNeedsAndRiskScores.individualNeedsScores.individualSelfManagementScores.overallSelfManagementScore()
    val overallNeedsScore = listOf(sexDomainScore, thinkingDomainScore, relationshipDomainScore, selfManagementDomainScore).sum()
    return NeedsScore(
      overallNeedsScore = overallNeedsScore,
      classification = getClassification(overallNeedsScore),
      domainScore = DomainScore(
        sexDomainScore = SexDomainScore(
          overAllSexDomainScore = sexDomainScore,
          individualSexScores = individualNeedsAndRiskScores.individualNeedsScores.individualSexScores,
        ),
        thinkingDomainScore = ThinkingDomainScore(
          overallThinkingDomainScore = thinkingDomainScore,
          individualThinkingScores = individualNeedsAndRiskScores.individualNeedsScores.individualCognitiveScores,
        ),
        relationshipDomainScore = RelationshipDomainScore(
          overallRelationshipDomainScore = relationshipDomainScore,
          individualRelationshipScores = individualNeedsAndRiskScores.individualNeedsScores.individualRelationshipScores,
        ),
        selfManagementDomainScore = SelfManagementDomainScore(
          overallSelfManagementDomainScore = selfManagementDomainScore,
          individualSelfManagementScores = individualNeedsAndRiskScores.individualNeedsScores.individualSelfManagementScores,
        ),
      ),
    )
  }

  fun getSexDomainScore(individualNeedsAndRiskScores: IndividualNeedsAndRiskScores, prisonNumber: String): Int {
    val hasNullValues = individualNeedsAndRiskScores.individualNeedsScores.individualSexScores.hasNullValues()

    val totalSexScore = if (hasNullValues) {
      if ((
        individualNeedsAndRiskScores.riskScores.ospDc?.let { it > BigDecimal.ZERO } == true ||
          individualNeedsAndRiskScores.riskScores.ospIic?.let { it > BigDecimal.ZERO } == true
        )
      ) {
        throw BusinessException("PNI information cannot be computed for $prisonNumber as ospDC and OspII scores are present but sexScore contains null")
      } else {
        ZERO
      }
    } else {
      individualNeedsAndRiskScores.individualNeedsScores.individualSexScores.totalScore()
    }

    return individualNeedsAndRiskScores.individualNeedsScores.individualSexScores.overallSexDomainScore(totalSexScore)
  }

  private fun getClassification(overallNeedsScore: Int): String {
    return when (overallNeedsScore) {
      in 0..2 -> "LOW NEED"
      in 3..5 -> "MEDIUM NEED"
      in 6..8 -> "HIGH NEED"
      else -> throw BusinessException("Unable to compute classification. Overall needs score is $overallNeedsScore")
    }
  }
}
