package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param temperControl
 * @param problemSolvingSkills
 * @param awarenessOfConsequences
 * @param achieveGoals
 * @param understandsViewsOfOthers
 * @param concreteAbstractThinking
 * @param sexualPreOccupation
 * @param offenceRelatedSexualInterests
 * @param aggressiveControllingBehaviour
 * @param impulsivity
 */
data class Behaviour(

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("temperControl") val temperControl: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("problemSolvingSkills") val problemSolvingSkills: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("awarenessOfConsequences") val awarenessOfConsequences: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("achieveGoals") val achieveGoals: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("understandsViewsOfOthers") val understandsViewsOfOthers: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("concreteAbstractThinking") val concreteAbstractThinking: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("sexualPreOccupation") val sexualPreOccupation: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("offenceRelatedSexualInterests") val offenceRelatedSexualInterests: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("aggressiveControllingBehaviour") val aggressiveControllingBehaviour: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("impulsivity") val impulsivity: String? = null,
)
