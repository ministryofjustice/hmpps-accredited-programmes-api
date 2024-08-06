package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param description
 * @param sentenceStartDate
 */
data class Sentence(

  @Schema(example = "CJA03 Standard Determinate Sentence", description = "")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("sentenceStartDate") val sentenceStartDate: java.time.LocalDate? = null,
)
