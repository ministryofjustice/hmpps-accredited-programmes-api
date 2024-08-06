package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param name The name of this Course Prerequisite.
 * @param course The name of the Course to which this Prerequisite applies. The name must match a course name exactly for this Prerequisite to be added to the Course.
 * @param identifier
 * @param description The value of this Course Prerequisite.
 * @param comments
 */
data class PrerequisiteRecord(

  @Schema(example = "age", required = true, description = "The name of this Course Prerequisite.")
  @get:JsonProperty("name", required = true) val name: kotlin.String,

  @Schema(example = "Kaizen", required = true, description = "The name of the Course to which this Prerequisite applies. The name must match a course name exactly for this Prerequisite to be added to the Course.")
  @get:JsonProperty("course", required = true) val course: kotlin.String,

  @Schema(example = "BNM-IPVO", required = true, description = "")
  @get:JsonProperty("identifier", required = true) val identifier: kotlin.String,

  @Schema(example = "18+", description = "The value of this Course Prerequisite.")
  @get:JsonProperty("description") val description: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("comments") val comments: kotlin.String? = null,
)
