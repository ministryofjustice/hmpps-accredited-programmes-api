package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param description
 * @param difficultiesCoping
 * @param currPsychologicalProblems
 * @param selfHarmSuicidal
 */
data class Psychiatric(

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("difficultiesCoping") val difficultiesCoping: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("currPsychologicalProblems") val currPsychologicalProblems: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("selfHarmSuicidal") val selfHarmSuicidal: kotlin.String? = null,
)
