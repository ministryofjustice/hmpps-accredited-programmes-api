package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param content
 */
data class SAR(

  @Schema(example = "null", description = "")
  @get:JsonProperty("content") val content: String? = null,
)
