package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param individualSexScores
 * @param individualCognitiveScores
 * @param individualRelationshipScores
 * @param individualSelfManagementScores
 */
data class IndividualNeedsScores(

  @get:JsonProperty("IndividualSexScores") val individualSexScores: IndividualSexScores,

  @get:JsonProperty("IndividualCognitiveScores") val individualCognitiveScores: IndividualCognitiveScores,

  @get:JsonProperty("IndividualRelationshipScores") val individualRelationshipScores: IndividualRelationshipScores,

  @get:JsonProperty("IndividualSelfManagementScores") val individualSelfManagementScores: IndividualSelfManagementScores,
)

data class IndividualSexScores(

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
    totalScore in 4..6 || (offenceRelatedSexualInterests == 2) -> 2
    totalScore in 0..1 -> 0
    totalScore in 2..3 -> 1
    else -> 0
  }
}

data class IndividualCognitiveScores(

  @Schema(example = "2", description = "")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: kotlin.Int? = null,

  @Schema(example = "2", description = "")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: kotlin.Int? = null,
) {
  fun totalScore(): Int {
    return (proCriminalAttitudes ?: 0) +
      (hostileOrientation ?: 0)
  }

  fun overallCognitiveDomainScore(): Int {
    val totalScore = totalScore()

    return when {
      totalScore in 3..4 || (proCriminalAttitudes == 2) -> 2
      totalScore in 1..2 -> 1
      else -> 0
    }
  }
}

data class IndividualSelfManagementScores(
  @Schema(example = "2", description = "")
  @get:JsonProperty("impulsivity") val impulsivity: kotlin.Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("temperControl") val temperControl: kotlin.Int? = null,

  @Schema(example = "0", description = "")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: kotlin.Int? = null,

  @Schema(example = "", description = "")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: kotlin.Int? = null,
) {
  fun totalScore(): Int {
    return (impulsivity ?: 0) +
      (temperControl ?: 0) +
      (problemSolvingSkills ?: 0) +
      (difficultiesCoping ?: 0)
  }

  fun overallSelfManagementScore() =
    when (totalScore()) {
      in 0..1 -> 0
      in 2..4 -> 1
      in 5..8 -> 2
      else -> 0
    }
}

data class IndividualRelationshipScores(

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

  fun overallRelationshipScore() = when (totalScore()) {
    in 0..1 -> 0
    in 2..4 -> 1
    in 5..8 -> 2
    else -> 0
  }
}
