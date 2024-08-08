package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param name
 * @param description
 */
data class CoursePrerequisite(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("name", required = true) val name: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: String,
)
