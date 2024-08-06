package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param activitiesEncourageOffending
 * @param lifestyleIssues
 * @param easilyInfluenced
 */
data class Lifestyle(

  @Schema(example = "Drug addiction", description = "")
  @get:JsonProperty("activitiesEncourageOffending") val activitiesEncourageOffending: kotlin.String? = null,

  @Schema(example = "Commits robbery to fund drug addiction", description = "")
  @get:JsonProperty("lifestyleIssues") val lifestyleIssues: kotlin.String? = null,

  @Schema(example = "1-Some problems", description = "")
  @get:JsonProperty("easilyInfluenced") val easilyInfluenced: kotlin.String? = null,
)
