package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param category
 */
data class Category(

  @Schema(example = "E", required = true, description = "")
  @get:JsonProperty("category", required = true) val category: String,
)
