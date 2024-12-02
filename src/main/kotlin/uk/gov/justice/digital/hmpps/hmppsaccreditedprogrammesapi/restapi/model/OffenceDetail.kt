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
  @get:JsonProperty("offenceDetails") val offenceDetails: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("contactTargeting") val contactTargeting: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("raciallyMotivated") val raciallyMotivated: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("revenge") val revenge: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("domesticViolence") val domesticViolence: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("repeatVictimisation") val repeatVictimisation: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimWasStranger") val victimWasStranger: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("stalking") val stalking: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("recognisesImpact") val recognisesImpact: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("numberOfOthersInvolved") val numberOfOthersInvolved: String? = null,

  @Schema(example = "There were two others involved who absconded at the scene", description = "")
  @get:JsonProperty("othersInvolvedDetail") val othersInvolvedDetail: String? = null,

  @Schema(example = "This person is easily lead", description = "")
  @get:JsonProperty("peerGroupInfluences") val peerGroupInfluences: String? = null,

  @Schema(example = "Drug misuse fuels this persons motivation", description = "")
  @get:JsonProperty("motivationAndTriggers") val motivationAndTriggers: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("acceptsResponsibility") val acceptsResponsibility: Boolean? = false,

  @Schema(example = "This person fully accepts their actions", description = "")
  @get:JsonProperty("acceptsResponsibilityDetail") val acceptsResponsibilityDetail: String? = null,

  @Schema(example = "This person has a long history of robbery", description = "")
  @get:JsonProperty("patternOffending") val patternOffending: String? = null,
)
