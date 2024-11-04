package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.reflect.full.memberProperties

// Extension function to check if all properties of a data class are non-null
fun <T : Any> T.areAllValuesPresent(): Boolean {
  return this::class.memberProperties
    .all { property -> property.call(this) != null }
}

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
  @get:JsonProperty("sexualPreOccupation") val sexualPreOccupation: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("offenceRelatedSexualInterests") val offenceRelatedSexualInterests: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: Int? = null,
) {

  @JsonIgnore
  fun hasSomeDataPresent() = listOf(
    sexualPreOccupation,
    offenceRelatedSexualInterests,
    emotionalCongruence,
  ).any { it != null }

  @JsonIgnore
  fun totalScore(): Int {
    return (sexualPreOccupation ?: 0) +
      (offenceRelatedSexualInterests ?: 0) +
      (emotionalCongruence ?: 0)
  }

  @JsonIgnore
  fun overallSexDomainScore(totalScore: Int?) = when {
    !areAllValuesPresent() -> null
    totalScore == 0 -> null
    totalScore in 4..6 || (offenceRelatedSexualInterests == 2) -> 2
    totalScore in 0..1 -> 0
    totalScore in 2..3 -> 1
    else -> null
  }
}

data class IndividualCognitiveScores(

  @Schema(example = "2", description = "")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: Int? = null,

  @Schema(example = "2", description = "")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: Int? = null,
) {
  fun totalScore(): Int {
    return (proCriminalAttitudes ?: 0) +
      (hostileOrientation ?: 0)
  }

  fun overallCognitiveDomainScore(): Int? {
    if (!areAllValuesPresent()) return null
    val totalScore = totalScore()

    return when {
      totalScore == 0 -> null
      totalScore in 3..4 || (proCriminalAttitudes == 2) -> 2
      totalScore in 1..2 -> 1
      else -> null
    }
  }
}

data class IndividualSelfManagementScores(
  @Schema(example = "2", description = "")
  @get:JsonProperty("impulsivity") val impulsivity: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("temperControl") val temperControl: Int? = null,

  @Schema(example = "0", description = "")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: Int? = null,

  @Schema(example = "", description = "")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: Int? = null,
) {
  fun totalScore(): Int {
    return (impulsivity ?: 0) +
      (temperControl ?: 0) +
      (problemSolvingSkills ?: 0) +
      (difficultiesCoping ?: 0)
  }

  fun overallSelfManagementScore(): Int? {
    if (!areAllValuesPresent()) return null

    return when (totalScore()) {
      0 -> null
      in 0..1 -> 0
      in 2..4 -> 1
      in 5..8 -> 2
      else -> null
    }
  }
}

data class IndividualRelationshipScores(

  @Schema(example = "1", description = "")
  @get:JsonProperty("curRelCloseFamily") val curRelCloseFamily: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("prevExpCloseRel") val prevExpCloseRel: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("easilyInfluenced") val easilyInfluenced: Int? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("aggressiveControllingBehaviour") val aggressiveControllingBehaviour: Int? = null,
) {

  fun totalScore(): Int {
    return (curRelCloseFamily ?: 0) +
      (prevExpCloseRel ?: 0) +
      (easilyInfluenced ?: 0) +
      (aggressiveControllingBehaviour ?: 0)
  }

  fun overallRelationshipScore(): Int? {
    if (!areAllValuesPresent()) return null

    return when (totalScore()) {
      0 -> null
      in 0..1 -> 0
      in 2..4 -> 1
      in 5..8 -> 2
      else -> null
    }
  }
}
