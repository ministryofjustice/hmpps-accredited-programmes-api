package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param description
 */
data class EnabledOrganisation(

  @Schema(example = "MDI", description = "")
  @get:JsonProperty("code") val code: String? = null,

  @Schema(example = "Stocken", description = "")
  @get:JsonProperty("description") val description: String? = null,
)
