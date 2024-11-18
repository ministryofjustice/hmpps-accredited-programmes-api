package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk

data class Sara(
  @Schema(example = "LOW", description = "The overall SARA risk score")
  @get:JsonProperty("sara") val overallResult: SaraRisk? = null,

  @Schema(example = "LOW", description = "Risk of violence towards partner")
  @get:JsonProperty("saraRiskOfViolenceTowardsPartner") val saraRiskOfViolenceTowardsPartner: String? = null,

  @Schema(example = "LOW", description = "Risk of violence towards others")
  @get:JsonProperty("saraRiskOfViolenceTowardsOthers") val saraRiskOfViolenceTowardsOthers: String? = null,

  @Schema(example = "2512235167", description = "Assessment ID relevant to the SARA version of the assessment")
  @get:JsonProperty("saraAssessmentId") val saraAssessmentId: Long? = null,
)