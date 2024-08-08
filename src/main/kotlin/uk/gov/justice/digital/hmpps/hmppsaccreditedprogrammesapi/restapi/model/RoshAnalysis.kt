package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offenceDetails
 * @param whereAndWhen
 * @param howDone
 * @param whoVictims
 * @param anyoneElsePresent
 * @param whyDone
 * @param sources
 */
data class RoshAnalysis(

  @Schema(example = "Tax evasion", description = "")
  @get:JsonProperty("offenceDetails") val offenceDetails: String? = null,

  @Schema(example = "at home", description = "")
  @get:JsonProperty("whereAndWhen") val whereAndWhen: String? = null,

  @Schema(example = "false accounting", description = "")
  @get:JsonProperty("howDone") val howDone: String? = null,

  @Schema(example = "hmrc", description = "")
  @get:JsonProperty("whoVictims") val whoVictims: String? = null,

  @Schema(example = "company secretary", description = "")
  @get:JsonProperty("anyoneElsePresent") val anyoneElsePresent: String? = null,

  @Schema(example = "Greed", description = "")
  @get:JsonProperty("whyDone") val whyDone: String? = null,

  @Schema(example = "crown court", description = "")
  @get:JsonProperty("sources") val sources: String? = null,
)
