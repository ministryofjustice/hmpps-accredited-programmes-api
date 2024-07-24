package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param needsScores
 * @param riskScores
 */
data class PNIInfo(

  @Schema(example = "null", description = "")
  @get:JsonProperty("Needs") val needsScores: NeedsScores? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("RiskScores") val riskScores: RiskScores? = null,
)
