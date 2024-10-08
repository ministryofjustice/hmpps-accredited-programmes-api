package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import java.math.BigDecimal

@Service
class PniRiskEngine {
  fun getOverallRiskScore(individualRiskScores: IndividualRiskScores, prisonNumber: String, gender: String): RiskScore {
    val classification = getRiskClassification(individualRiskScores, gender)

    return RiskScore(
      classification = classification,
      individualRiskScores = individualRiskScores,
    )
  }

  fun isHighIntensityBasedOnRiskScores(individualRiskScores: IndividualRiskScores) =
    (isHighOgrs3(individualRiskScores) && (isHighOvp(individualRiskScores) || isHighSara(individualRiskScores)))

  fun getRiskClassification(individualRiskScores: IndividualRiskScores, gender: String) = when {
    isHighRisk(individualRiskScores, gender) -> RiskClassification.HIGH_RISK
    isMediumRisk(individualRiskScores, gender) -> RiskClassification.MEDIUM_RISK
    else -> RiskClassification.LOW_RISK
  }.name

  fun isHighRisk(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return (
      isHighOgrs3(individualRiskScores) ||
        isHighOvp(individualRiskScores) ||
        isOspDcHigh(individualRiskScores, gender) ||
        isOspIicHigh(individualRiskScores, gender) ||
        isRsrHigh(individualRiskScores, gender)
      ) ||
      isHighSara(individualRiskScores)
  }

  fun isMediumRisk(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return (
      isOgrs3Medium(individualRiskScores) ||
        isOvpMedium(individualRiskScores) ||
        isOspDcMedium(individualRiskScores, gender) ||
        isOspIicMedium(individualRiskScores, gender) ||
        isRsrMedium(individualRiskScores, gender)
      ) ||
      (isMediumSara(individualRiskScores))
  }

  private fun isHighOgrs3(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ogrs3?.let { it > BigDecimal("75.00") } == true

  private fun isHighOvp(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ovp?.let { it > BigDecimal("60.00") } == true

  private fun isOspDcHigh(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return gender.equals("Male", ignoreCase = true) &&
      (
        individualRiskScores.ospDc?.equals("HIGH", ignoreCase = true) == true ||
          individualRiskScores.ospDc?.equals("VERY_HIGH", ignoreCase = true) == true
        )
  }

  private fun isOspIicHigh(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return gender.equals("Male", ignoreCase = true) &&
      (individualRiskScores.ospIic?.equals("HIGH", ignoreCase = true) == true) ||
      (individualRiskScores.ospIic?.equals("VERY_HIGH", ignoreCase = true) == true)
  }

  fun isHighSara(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.sara?.equals("High", ignoreCase = true) == true

  private fun isRsrHigh(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    val isHighRsr = individualRiskScores.rsr?.let { it >= BigDecimal("3.00") } == true

    if (gender.equals("Female", ignoreCase = true)) {
      return isHighRsr
    }
    return isHighRsr && (individualRiskScores.ospDc == null && individualRiskScores.ospIic == null)
  }

  private fun isOgrs3Medium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ogrs3?.let { it in BigDecimal("50.00")..BigDecimal("74.00") } == true

  private fun isOvpMedium(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.ovp?.let { it in BigDecimal("30.00")..BigDecimal("59.00") } == true

  private fun isOspDcMedium(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return gender.equals("Male", ignoreCase = true) && individualRiskScores.ospDc?.equals("MEDIUM", ignoreCase = true) == true
  }

  private fun isOspIicMedium(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    return gender.equals("Male", ignoreCase = true) && individualRiskScores.ospIic?.equals("MEDIUM", ignoreCase = true) == true
  }

  fun isMediumSara(individualRiskScores: IndividualRiskScores) =
    individualRiskScores.sara?.equals("Medium", ignoreCase = true) == true

  private fun isRsrMedium(individualRiskScores: IndividualRiskScores, gender: String): Boolean {
    val rsrMediumRsr = individualRiskScores.rsr?.let { it in BigDecimal("1.00")..BigDecimal("2.99") } == true
    if (gender.equals("Female", ignoreCase = true)) {
      return rsrMediumRsr
    }
    // osp scores needs to be ignored for females
    return rsrMediumRsr && individualRiskScores.ospDc == null && individualRiskScores.ospIic == null
  }
}

enum class RiskClassification {
  HIGH_RISK,
  MEDIUM_RISK,
  LOW_RISK,
}
