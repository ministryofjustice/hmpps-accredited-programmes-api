package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

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
data class SaraRiskLevel(val toPartner: Int, val toOther: Int)

enum class Type {
  H,
  M,
  A,
  O,
}
enum class Level {
  H,
  M,
  L,
}
