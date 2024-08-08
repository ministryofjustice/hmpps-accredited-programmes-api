package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param name
 * @param description
 * @param audienceId
 * @param withdrawn
 * @param identifier This is an internal identifier for the course (to be removed)
 * @param alternateName
 */
data class CourseCreateRequest(

  @Schema(example = "Thinking skills programme", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: String,

  @Schema(example = "Thinking Skills Programme (TSP) description", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: String,

  @Schema(example = "e4d1a44a-9c3b-4a7c-b79c-4d8a76488eb2", required = true, description = "")
  @get:JsonProperty("audienceId", required = true) val audienceId: UUID,

  @Schema(example = "true", required = true, description = "")
  @get:JsonProperty("withdrawn", required = true) val withdrawn: Boolean,

  @Schema(example = "BNM-VO", description = "This is an internal identifier for the course (to be removed)")
  @get:JsonProperty("identifier") val identifier: String? = null,

  @Schema(example = "BNM+", description = "")
  @get:JsonProperty("alternateName") val alternateName: String? = null,
)
