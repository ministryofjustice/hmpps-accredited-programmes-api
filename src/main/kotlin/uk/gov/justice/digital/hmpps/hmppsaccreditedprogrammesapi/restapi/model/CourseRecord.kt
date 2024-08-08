package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param name
 * @param identifier
 * @param description
 * @param audience
 * @param alternateName
 * @param audienceColour
 * @param comments
 */
data class CourseRecord(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("identifier", required = true) val identifier: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("audience", required = true) val audience: String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("alternateName") val alternateName: String? = null,

  @Schema(example = "purple", description = "")
  @get:JsonProperty("audienceColour") val audienceColour: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("comments") val comments: String? = null,
)
