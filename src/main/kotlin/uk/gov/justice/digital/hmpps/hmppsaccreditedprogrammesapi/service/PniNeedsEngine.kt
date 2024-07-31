package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.PNIInfo
import java.math.BigDecimal

private const val ZERO = 0

@Service
class PniNeedsEngine {

  fun getOverallNeedsScore(pniInfo: PNIInfo, prisonNumber: String): NeedsScore {
    val sexDomainScore = getSexDomainScore(pniInfo, prisonNumber)
    val thinkingDomainScore = pniInfo.individualNeedsScores.cognitiveScores.overallCognitiveDomainScore(prisonNumber)
    val relationshipDomainScore = pniInfo.individualNeedsScores.relationshipScores.overallRelationshipScore(prisonNumber)
    val selfManagementDomainScore = pniInfo.individualNeedsScores.selfManagementScores.overallSelfManagementScore(prisonNumber)

    return NeedsScore(
      overallNeedsScore = listOf(sexDomainScore, thinkingDomainScore, relationshipDomainScore, selfManagementDomainScore).sum(),
      domainScore = DomainScore(
        sexDomainScore = sexDomainScore,
        thinkingDomainScore = thinkingDomainScore,
        relationshipDomainScore = relationshipDomainScore,
        selfManagementDomainScore = selfManagementDomainScore,
      ),
    )
  }

  fun getSexDomainScore(pniInfo: PNIInfo, prisonNumber: String): Int {
    val hasNullValues = pniInfo.individualNeedsScores.sexScores.hasNullValues()

    val totalSexScore = if (hasNullValues) {
      if ((
        pniInfo.riskScores.ospDc?.let { it > BigDecimal.ZERO } == true ||
          pniInfo.riskScores.ospIic?.let { it > BigDecimal.ZERO } == true
        )
      ) {
        throw BusinessException("PNI information cannot be computed for $prisonNumber as ospDC and OspII scores are present but sexScore contains null")
      } else {
        ZERO
      }
    } else {
      pniInfo.individualNeedsScores.sexScores.totalScore()
    }

    return pniInfo.individualNeedsScores.sexScores.overallSexDomainScore(totalSexScore)
  }
}
