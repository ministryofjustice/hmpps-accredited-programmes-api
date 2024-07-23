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
data class NeedsScores(

  @Schema(example = "null", description = "")
    @get:JsonProperty("Sex") val sexScores: SexScores? = null,

  @Schema(example = "null", description = "")
    @get:JsonProperty("Cognitive") val cognitiveScores: CognitiveScores? = null,

  @Schema(example = "null", description = "")
    @get:JsonProperty("Relationships") val relationshipScores: RelationshipScores? = null,

  @Schema(example = "null", description = "")
    @get:JsonProperty("SelfManagement") val selfManagementScores: SelfManagementScores? = null
)

data class SexScores(

  @Schema(example = "null", description = "")
  @get:JsonProperty("sexualPreOccupation") val sexualPreOccupation: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("offenceRelatedSexualInterests") val offenceRelatedSexualInterests: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: kotlin.Int? = null
)

data class CognitiveScores(

  @Schema(example = "null", description = "")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: kotlin.Int? = null
)


class SelfManagementScores(
  @Schema(example = "null", description = "")
  @get:JsonProperty("impulsivity") val impulsivity: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("temperControl") val temperControl: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: kotlin.Int? = null
)

data class RelationshipScores(

  @Schema(example = "null", description = "")
  @get:JsonProperty("curRelCloseFamily") val curRelCloseFamily: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("prevExpCloseRel") val prevExpCloseRel: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("easilyInfluenced") val easilyInfluenced: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("aggressiveControllingBehaviour") val aggressiveControllingBehaviour: kotlin.Int? = null
)