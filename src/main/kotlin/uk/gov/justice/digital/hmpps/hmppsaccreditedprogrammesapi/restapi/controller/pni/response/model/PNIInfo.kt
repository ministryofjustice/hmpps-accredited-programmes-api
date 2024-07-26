package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param needsScores
 * @param riskScores
 */
data class PNIInfo(

  @Schema(
    example = "" +
      "{ " +
      "sexScores=SexScores(sexualPreOccupation=null, offenceRelatedSexualInterests=null, emotionalCongruence=0), " +
      "cognitiveScores=CognitiveScores(proCriminalAttitudes=1, hostileOrientation=null), " +
      "relationshipScores=RelationshipScores(curRelCloseFamily=null, prevExpCloseRel=2, easilyInfluenced=null, aggressiveControllingBehaviour=null), " +
      "selfManagementScores=SelfManagementScores(impulsivity=null, temperControl=null, problemSolvingSkills=null, difficultiesCoping=null)" +
      "}",
    description = "",
  )
  @get:JsonProperty("Needs") val needsScores: NeedsScores? = null,

  @Schema(example = "{ogrs3=8, ovp=8, ospDc=1.07, ospIic=0.11, rsr=1.46, sara=High}", description = "")
  @get:JsonProperty("RiskScores") val riskScores: RiskScores? = null,
)