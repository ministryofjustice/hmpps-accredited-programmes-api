package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param alcoholLinkedToHarm
 * @param alcoholIssuesDetails
 * @param frequencyAndLevel
 * @param bingeDrinking
 */
data class OasysAlcoholDetail(

  @Schema(example = "null", description = "")
  @get:JsonProperty("alcoholLinkedToHarm") val alcoholLinkedToHarm: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("alcoholIssuesDetails") val alcoholIssuesDetails: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("frequencyAndLevel") val frequencyAndLevel: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("bingeDrinking") val bingeDrinking: kotlin.String? = null,
)
