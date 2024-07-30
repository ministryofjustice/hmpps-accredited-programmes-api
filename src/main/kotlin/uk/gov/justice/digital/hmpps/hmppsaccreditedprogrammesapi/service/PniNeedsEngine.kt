package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.PNIInfo
import java.math.BigDecimal

private const val ZERO = 0

@Service
class PniNeedsEngine {

  fun getOverallNeedsScore(pniInfo: PNIInfo, prisonNumber: String): Int {
    val sexDomainScore = getSexDomainScore(pniInfo, prisonNumber)

    return sexDomainScore
  }

  fun getSexDomainScore(pniInfo: PNIInfo, prisonNumber: String): Int {
    val hasNullValues = pniInfo.needsScores.sexScores.hasNullValues()

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
      pniInfo.needsScores.sexScores.totalScore()
    }

    return pniInfo.needsScores.sexScores.overallSexDomainScore(totalSexScore)
  }
}
