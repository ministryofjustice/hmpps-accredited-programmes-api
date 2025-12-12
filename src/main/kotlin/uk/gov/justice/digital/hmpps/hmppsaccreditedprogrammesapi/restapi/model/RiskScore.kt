package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class RiskScore(

  @Schema(example = "High Risk", description = "")
  @get:JsonProperty("classification") val classification: String,

  @Schema(example = "2", description = "")
  @get:JsonProperty("IndividualRiskScores") val individualRiskScores: IndividualRiskScores,
)
