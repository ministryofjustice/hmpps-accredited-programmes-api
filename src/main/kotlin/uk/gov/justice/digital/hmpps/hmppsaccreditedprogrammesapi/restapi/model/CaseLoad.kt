package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param caseLoadId The unique identifier of the caseload.
 * @param description The description of the caseload.
 * @param type The type of the caseload.
 * @param caseloadFunction The function of the caseload.
 * @param currentlyActive Indicates whether the caseload is currently active or not.
 */
data class CaseLoad(

  @Schema(example = "null", description = "The unique identifier of the caseload.")
  @get:JsonProperty("caseLoadId") val caseLoadId: kotlin.String? = null,

  @Schema(example = "null", description = "The description of the caseload.")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "null", description = "The type of the caseload.")
  @get:JsonProperty("type") val type: kotlin.String? = null,

  @Schema(example = "null", description = "The function of the caseload.")
  @get:JsonProperty("caseloadFunction") val caseloadFunction: kotlin.String? = null,

  @Schema(example = "null", description = "Indicates whether the caseload is currently active or not.")
  @get:JsonProperty("currentlyActive") val currentlyActive: kotlin.Boolean? = null,
)
