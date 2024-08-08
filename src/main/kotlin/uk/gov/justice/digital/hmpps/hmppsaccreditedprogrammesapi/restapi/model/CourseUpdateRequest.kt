package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param name
 * @param description
 * @param alternateName
 * @param displayName
 * @param audience
 * @param audienceColour
 * @param withdrawn
 */
data class CourseUpdateRequest(

  @Schema(example = "Thinking skills programme", description = "")
  @get:JsonProperty("name") val name: kotlin.String? = null,

  @Schema(example = "Thinking Skills Programme (TSP) description", description = "")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "BNM+", description = "")
  @get:JsonProperty("alternateName") val alternateName: kotlin.String? = null,

  @Schema(example = "Becoming New Me Plus: general violence offence (BNM+)", description = "")
  @get:JsonProperty("displayName") val displayName: kotlin.String? = null,

  @Schema(example = "Gang offence", description = "")
  @get:JsonProperty("audience") val audience: kotlin.String? = null,

  @Schema(example = "#FF5733", description = "")
  @get:JsonProperty("audienceColour") val audienceColour: kotlin.String? = null,

  @Schema(example = "true", description = "")
  @get:JsonProperty("withdrawn") val withdrawn: kotlin.Boolean? = null,
)
