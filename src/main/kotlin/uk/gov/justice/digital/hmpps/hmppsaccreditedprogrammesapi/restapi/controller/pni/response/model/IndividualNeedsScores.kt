package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param sexScores
 * @param cognitiveScores
 * @param relationshipScores
 * @param selfManagementScores
 */
data class IndividualNeedsScores(

  @get:JsonProperty("Sex") val sexScores: SexScores,

  @get:JsonProperty("Cognitive") val cognitiveScores: CognitiveScores,

  @get:JsonProperty("Relationships") val relationshipScores: RelationshipScores,

  @get:JsonProperty("SelfManagement") val selfManagementScores: SelfManagementScores,
)

data class SexScores(

  @Schema(example = "1", description = "")
  @get:JsonProperty("sexualPreOccupation") val sexualPreOccupation: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("offenceRelatedSexualInterests") val offenceRelatedSexualInterests: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: kotlin.Int? = null,
) {
  fun hasNullValues() = listOf(
    sexualPreOccupation,
    offenceRelatedSexualInterests,
    emotionalCongruence,
  ).any { it == null }

  fun totalScore(): Int {
    return (sexualPreOccupation ?: 0) +
      (offenceRelatedSexualInterests ?: 0) +
      (emotionalCongruence ?: 0)
  }

  fun overallSexDomainScore(totalScore: Int) = when {
    totalScore in 0..1 -> 0
    totalScore in 2..3 -> 1
    totalScore in 4..6 || (offenceRelatedSexualInterests == 2) -> 2
    else -> 0
  }
}

data class CognitiveScores(

  @Schema(example = "2", description = "")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: kotlin.Int? = null,

  @Schema(example = "2", description = "")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: kotlin.Int? = null,
) {
  private fun totalScore(): Int {
    return (proCriminalAttitudes ?: 0) +
      (hostileOrientation ?: 0)
  }

  fun hasNullValues() = listOf(
    proCriminalAttitudes,
    hostileOrientation,
  ).any { it == null }

  fun overallCognitiveDomainScore(prisonNumber: String): Int {
//    if (hasNullValues()) {
//      throw BusinessException("PNI information cannot be computed for $prisonNumber as ThinkingSkillsDomain contains null values")
//    }

    val totalScore = totalScore()

    return when {
      totalScore in 1..2 -> 1
      totalScore in 3..4 || (proCriminalAttitudes == 2) -> 2
      else -> 0
    }
  }
}

data class SelfManagementScores(
  @Schema(example = "2", description = "")
  @get:JsonProperty("impulsivity") val impulsivity: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("temperControl") val temperControl: kotlin.Int? = null,

  @Schema(example = "0", description = "")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: kotlin.Int? = null,

  @Schema(example = "", description = "")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: kotlin.Int? = null,
) {
  private fun totalScore(): Int {
    return (impulsivity ?: 0) +
      (temperControl ?: 0) +
      (problemSolvingSkills ?: 0) +
      (difficultiesCoping ?: 0)
  }

  fun hasNullValues() = listOf(
    impulsivity,
    temperControl,
    problemSolvingSkills,
    difficultiesCoping,
  ).any { it == null }

  fun overallSelfManagementScore(prisonNumber: String): Int {
//    if (hasNullValues()) {
//      throw BusinessException("PNI information cannot be computed for $prisonNumber as SelfManagementDomain contains null values")
//    }

    val totalScore = totalScore()

    return when {
      totalScore in 0..1 -> 0
      totalScore in 2..4 -> 1
      totalScore in 5..8 -> 2
      else -> 0
    }
  }
}

data class RelationshipScores(

  @Schema(example = "1", description = "")
  @get:JsonProperty("curRelCloseFamily") val curRelCloseFamily: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("prevExpCloseRel") val prevExpCloseRel: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("easilyInfluenced") val easilyInfluenced: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("aggressiveControllingBehaviour") val aggressiveControllingBehaviour: kotlin.Int? = null,
) {

  fun totalScore(): Int {
    return (curRelCloseFamily ?: 0) +
      (prevExpCloseRel ?: 0) +
      (easilyInfluenced ?: 0) +
      (aggressiveControllingBehaviour ?: 0)
  }

  private fun hasNullValues() = listOf(
    curRelCloseFamily,
    prevExpCloseRel,
    easilyInfluenced,
    aggressiveControllingBehaviour,
  ).any { it == null }

  fun overallRelationshipScore(prisonNumber: String): Int {
//    if (hasNullValues()) {
//      throw BusinessException("PNI information cannot be computed for $prisonNumber as RelationshipScore contains null values")
//    }

    val totalScore = totalScore()

    return when {
      totalScore in 0..1 -> 0
      totalScore in 2..4 -> 1
      totalScore in 5..8 -> 2
      else -> 0
    }
  }
}
