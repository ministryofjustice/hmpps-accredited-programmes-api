package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk

data class PniCalculation(
  val sexDomain: LevelScore,
  val thinkingDomain: LevelScore,
  val relationshipDomain: LevelScore,
  val selfManagementDomain: LevelScore,
  val riskLevel: Level,
  val needLevel: Level,
  val totalDomainScore: Int,
  val pni: Type,
  val saraRiskLevel: SaraRiskLevel,
  val missingFields: List<String> = listOf(),
)

data class LevelScore(val level: Level, val score: Int)
data class SaraRiskLevel(val toPartner: Int, val toOther: Int) {
  companion object {
    private val riskLevelMap = mapOf(
      1 to SaraRisk.LOW,
      2 to SaraRisk.MEDIUM,
      3 to SaraRisk.HIGH,
    )

    fun getRiskForPartner(toPartner: Int?): SaraRisk = getRiskFromMap(toPartner)
    fun getRiskToOthers(toOther: Int?): SaraRisk = getRiskFromMap(toOther)

    private fun getRiskFromMap(riskLevel: Int?): SaraRisk = riskLevelMap.getOrDefault(riskLevel, SaraRisk.NOT_APPLICABLE)
  }
}

enum class Type {
  H,
  M,
  A,
  O,
  ;

  companion object {
    fun toText(type: Type?): String = when (type) {
      H -> "HIGH_INTENSITY_BC"
      M -> "MODERATE_INTENSITY_BC"
      A -> "ALTERNATIVE_PATHWAY"
      O -> "MISSING_INFORMATION"
      else -> "MISSING_INFORMATION"
    }
  }
}
enum class Level {
  H,
  M,
  L,
  ;

  companion object {

    fun toNeedLevel(level: Level?): String = when (level) {
      H -> "HIGH_NEED"
      M -> "MEDIUM_NEED"
      L -> "LOW_NEED"
      else -> "Unknown"
    }

    fun toRiskLevel(level: Level?): String = when (level) {
      H -> "HIGH_RISK"
      M -> "MEDIUM_RISK"
      L -> "LOW_RISK"
      else -> "Unknown"
    }
  }
}
