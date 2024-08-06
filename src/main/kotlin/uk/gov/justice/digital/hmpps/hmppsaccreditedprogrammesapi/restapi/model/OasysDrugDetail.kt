package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param levelOfUseOfMainDrug
 * @param drugsMajorActivity
 */
data class OasysDrugDetail(

  @Schema(example = "null", description = "")
  @get:JsonProperty("levelOfUseOfMainDrug") val levelOfUseOfMainDrug: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("drugsMajorActivity") val drugsMajorActivity: kotlin.String? = null,
)
