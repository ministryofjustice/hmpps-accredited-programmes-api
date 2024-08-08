package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prerequisites
 */
data class CoursePrerequisites(

  @Schema(example = "null", description = "")
  @get:JsonProperty("prerequisites") val prerequisites: List<CoursePrerequisite>? = null,
)
