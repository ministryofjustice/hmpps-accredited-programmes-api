package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

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
 * @param displayOnPgmdir
 */
data class Course(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("id", required = true) val id: UUID,

  @Schema(example = "Thinking skills programme", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("coursePrerequisites", required = true) val coursePrerequisites: List<CoursePrerequisite>,

  @Schema(example = "Gang offence", required = true, description = "")
  @get:JsonProperty("audience", required = true) val audience: String,

  @Schema(example = "BNM-VO", description = "This is an internal identifier for the course (to be removed)")
  @get:JsonProperty("identifier") val identifier: String? = null,

  @Schema(example = "Thinking Skills Programme (TSP) description", description = "")
  @get:JsonProperty("description") val description: String? = null,

  @Schema(example = "BNM+", description = "")
  @get:JsonProperty("alternateName") val alternateName: String? = null,

  @Schema(example = "Becoming New Me Plus: general violence offence (BNM+)", description = "")
  @get:JsonProperty("displayName") val displayName: String? = null,

  @Schema(example = "purple", description = "")
  @get:JsonProperty("audienceColour") val audienceColour: String? = null,

  @Schema(example = "true", description = "")
  @get:JsonProperty("withdrawn") val withdrawn: Boolean? = null,

  @Schema(example = "true", description = "")
  @get:JsonProperty("displayOnPgmdir") val displayOnPgmdir: Boolean? = null,
)
