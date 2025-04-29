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

enum class ProgrammePathway {
  HIGH_INTENSITY_BC,
  MODERATE_INTENSITY_BC,
  ALTERNATIVE_PATHWAY,
  MISSING_INFORMATION,
}

enum class Type {
  H,
  M,
  A,
  O,
  ;

  companion object {
    fun toPathway(type: Type?): ProgrammePathway = when (type) {
      H -> ProgrammePathway.HIGH_INTENSITY_BC
      M -> ProgrammePathway.MODERATE_INTENSITY_BC
      A -> ProgrammePathway.ALTERNATIVE_PATHWAY
      O -> ProgrammePathway.MISSING_INFORMATION
      else -> ProgrammePathway.MISSING_INFORMATION
    }
  }
}

enum class NeedLevel {
  HIGH_NEED,
  MEDIUM_NEED,
  LOW_NEED,
  UNKNOWN,
  ;

  companion object {
    fun fromLevel(level: Level?): NeedLevel = when (level) {
      Level.H -> HIGH_NEED
      Level.M -> MEDIUM_NEED
      Level.L -> LOW_NEED
      else -> UNKNOWN
    }
  }
}

enum class Level {
  H,
  M,
  L,
}

enum class RiskLevel {
  HIGH_RISK,
  MEDIUM_RISK,
  LOW_RISK,
  UNKNOWN,
  ;

  companion object {
    fun fromLevel(level: Level?): RiskLevel = when (level) {
      Level.H -> HIGH_RISK
      Level.M -> MEDIUM_RISK
      Level.L -> LOW_RISK
      else -> UNKNOWN
    }
  }
}
