package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 *
 * @param description
 * @param sentenceStartDate
 */
data class Sentence(

  @Schema(example = "CJA03 Standard Determinate Sentence", description = "")
  @get:JsonProperty("description") val description: String? = null,

  @Schema(example = "2021-06-14", description = "The sentence start date")
  @get:JsonProperty("sentenceStartDate") val sentenceStartDate: LocalDate? = null,

  @Schema(example = "2028-02-11", description = "The sentence end date")
  @get:JsonProperty("sentenceEndDate") val sentenceEndDate: LocalDate? = null,
)
