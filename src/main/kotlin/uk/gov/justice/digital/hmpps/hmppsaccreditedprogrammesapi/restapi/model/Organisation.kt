package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param prisonName
 */
data class Organisation(

  @Schema(example = "MDI", description = "")
  @get:JsonProperty("code") val code: String? = null,

  @Schema(example = "Moorland HMP", description = "")
  @get:JsonProperty("prisonName") val prisonName: String? = null,
)
