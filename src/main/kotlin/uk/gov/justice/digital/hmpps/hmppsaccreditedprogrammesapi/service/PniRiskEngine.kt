package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import java.math.BigDecimal

@Service
class PniRiskEngine {
  fun getOverallRiskScore(individualRiskScores: IndividualRiskScores, prisonNumber: String): RiskScore {
    val classification = getRiskClassification(individualRiskScores)

    return RiskScore(
      classification = classification,
      individualRiskScores = individualRiskScores,
    )
  }

  fun isHighIntensityBasedOnRiskScores(individualRiskScores: IndividualRiskScores) =
    (isHighOgrs3(individualRiskScores) && (isHighOvp(individualRiskScores) || isHighSara(individualRiskScores)))

  fun getRiskClassification(individualRiskScores: IndividualRiskScores) = when {
    isHighRisk(individualRiskScores) -> RiskClassification.HIGH_RISK
    isMediumRisk(individualRiskScores) -> RiskClassification.MEDIUM_RISK
    else -> RiskClassification.LOW_RISK
  }.name

  fun isHighRisk(individualRiskScores: IndividualRiskScores): Boolean {
    return (
      isHighOgrs3(individualRiskScores) ||
        isHighOvp(individualRiskScores) ||
        isOspDcHigh(individualRiskScores) ||
        isOspIicHigh(individualRiskScores) ||
        isRsrHigh(individualRiskScores)
      ) ||
      isHighSara(individualRiskScores)
  }

  fun isMediumRisk(individualRiskScores: IndividualRiskScores): Boolean {
    return (
      isOgrs3Medium(individualRiskScores) ||
        isOvpMedium(individualRiskScores) ||
        isOspDcMedium(individualRiskScores) ||
        isOspIicMedium(individualRiskScores) ||
        isRsrMedium(individualRiskScores)
      ) ||
      (isMediumSara(individualRiskScores))
  }

  private fun isHighOgrs3(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ogrs3?.let { it > BigDecimal("75.00") } == true

  private fun isHighOvp(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ovp?.let { it > BigDecimal("60.00") } == true

  private fun isOspDcHigh(individualRiskScores: IndividualRiskScores) =
    (individualRiskScores.ospDc?.equals("HIGH", ignoreCase = true) == true) ||
      (individualRiskScores.ospDc?.equals("VERY_HIGH", ignoreCase = true) == true)

  private fun isOspIicHigh(individualRiskScores: IndividualRiskScores) =
    (individualRiskScores.ospIic?.equals("HIGH", ignoreCase = true) == true) ||
      (individualRiskScores.ospIic?.equals("VERY_HIGH", ignoreCase = true) == true)

  fun isHighSara(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.sara?.equals("High", ignoreCase = true) == true

  private fun isRsrHigh(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.rsr?.let { it >= BigDecimal("3.00") } == true &&
      (individualRiskScores.ospDc == null && individualRiskScores.ospIic == null)

  private fun isOgrs3Medium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ogrs3?.let { it in BigDecimal("50.00")..BigDecimal("74.00") } == true

  private fun isOvpMedium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ovp?.let { it in BigDecimal("30.00")..BigDecimal("59.00") } == true

  private fun isOspDcMedium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ospDc?.equals("MEDIUM", ignoreCase = true) == true

  private fun isOspIicMedium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ospIic?.equals("MEDIUM", ignoreCase = true) == true

  fun isMediumSara(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.sara?.equals("Medium", ignoreCase = true) == true

  private fun isRsrMedium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.rsr?.let { it in BigDecimal("1.00")..BigDecimal("2.99") } == true &&
      (individualRiskScores.ospDc == null && individualRiskScores.ospIic == null)
}

enum class RiskClassification {
  HIGH_RISK,
  MEDIUM_RISK,
  LOW_RISK,
}
