package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param recentCompletedAssessmentDate
 * @param hasOpenAssessment
 */
data class OasysAssessmentDateInfo(

  @Schema(example = "null", description = "")
  @get:JsonProperty("recentCompletedAssessmentDate") val recentCompletedAssessmentDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("hasOpenAssessment") val hasOpenAssessment: kotlin.Boolean? = null,
)
