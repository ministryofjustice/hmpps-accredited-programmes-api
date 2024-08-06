package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offenceDetails
 * @param contactTargeting
 * @param raciallyMotivated
 * @param revenge
 * @param domesticViolence
 * @param repeatVictimisation
 * @param victimWasStranger
 * @param stalking
 * @param recognisesImpact
 * @param numberOfOthersInvolved
 * @param othersInvolvedDetail
 * @param peerGroupInfluences
 * @param motivationAndTriggers
 * @param acceptsResponsibility
 * @param acceptsResponsibilityDetail
 * @param patternOffending
 */
data class OffenceDetail(

  @Schema(example = "Armed robbery", description = "")
  @get:JsonProperty("offenceDetails") val offenceDetails: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("contactTargeting") val contactTargeting: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("raciallyMotivated") val raciallyMotivated: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("revenge") val revenge: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("domesticViolence") val domesticViolence: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("repeatVictimisation") val repeatVictimisation: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimWasStranger") val victimWasStranger: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("stalking") val stalking: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("recognisesImpact") val recognisesImpact: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("numberOfOthersInvolved") val numberOfOthersInvolved: kotlin.Int? = 0,

  @Schema(example = "There were two others involved who absconded at the scene", description = "")
  @get:JsonProperty("othersInvolvedDetail") val othersInvolvedDetail: kotlin.String? = null,

  @Schema(example = "This person is easily lead", description = "")
  @get:JsonProperty("peerGroupInfluences") val peerGroupInfluences: kotlin.String? = null,

  @Schema(example = "Drug misuse fuels this persons motivation", description = "")
  @get:JsonProperty("motivationAndTriggers") val motivationAndTriggers: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("acceptsResponsibility") val acceptsResponsibility: kotlin.Boolean? = false,

  @Schema(example = "This person fully accepts their actions", description = "")
  @get:JsonProperty("acceptsResponsibilityDetail") val acceptsResponsibilityDetail: kotlin.String? = null,

  @Schema(example = "This person has a long history of robbery", description = "")
  @get:JsonProperty("patternOffending") val patternOffending: kotlin.String? = null,
)
