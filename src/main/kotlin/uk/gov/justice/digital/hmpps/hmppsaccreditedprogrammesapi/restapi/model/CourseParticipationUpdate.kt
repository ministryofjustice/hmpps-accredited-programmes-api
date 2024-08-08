package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param courseName The name of the course taken by the participant.
 * @param setting
 * @param outcome
 * @param detail
 * @param source
 */
data class CourseParticipationUpdate(

  @Schema(example = "null", description = "The name of the course taken by the participant.")
  @get:JsonProperty("courseName") val courseName: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("setting") val setting: CourseParticipationSetting? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("outcome") val outcome: CourseParticipationOutcome? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("detail") val detail: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("source") val source: String? = null,
)
