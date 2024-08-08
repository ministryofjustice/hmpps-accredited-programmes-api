package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param id
 * @param name
 * @param coursePrerequisites
 * @param audience
 * @param identifier This is an internal identifier for the course (to be removed)
 * @param description
 * @param alternateName
 * @param displayName
 * @param audienceColour
 * @param withdrawn
 */
data class Course(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("id", required = true) val id: java.util.UUID,

  @Schema(example = "Thinking skills programme", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: kotlin.String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("coursePrerequisites", required = true) val coursePrerequisites: kotlin.collections.List<CoursePrerequisite>,

  @Schema(example = "Gang offence", required = true, description = "")
  @get:JsonProperty("audience", required = true) val audience: kotlin.String,

  @Schema(example = "BNM-VO", description = "This is an internal identifier for the course (to be removed)")
  @get:JsonProperty("identifier") val identifier: kotlin.String? = null,

  @Schema(example = "Thinking Skills Programme (TSP) description", description = "")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "BNM+", description = "")
  @get:JsonProperty("alternateName") val alternateName: kotlin.String? = null,

  @Schema(example = "Becoming New Me Plus: general violence offence (BNM+)", description = "")
  @get:JsonProperty("displayName") val displayName: kotlin.String? = null,

  @Schema(example = "purple", description = "")
  @get:JsonProperty("audienceColour") val audienceColour: kotlin.String? = null,

  @Schema(example = "true", description = "")
  @get:JsonProperty("withdrawn") val withdrawn: kotlin.Boolean? = null,
)
