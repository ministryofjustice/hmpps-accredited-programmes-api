package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param prisonNumber The prison number of the course participant.
 * @param id A unique identifier for this record of participation in a course.
 * @param addedBy The identity of the person who added this CourseParticipation
 * @param createdAt The date and time at which this CourseParticipation was created. ISO 8601 date-time format.
 * @param courseName The name of the course taken by the participant.
 * @param setting
 * @param outcome
 * @param detail
 * @param source
 */
data class CourseParticipation(

  @Schema(example = "A1234AA", required = true, description = "The prison number of the course participant.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

  @Schema(example = "null", required = true, description = "A unique identifier for this record of participation in a course.")
  @get:JsonProperty("id", required = true) val id: UUID,

  @Schema(example = "null", required = true, description = "The unique identifier for the associated referral.")
  @get:JsonProperty("referralId", required = true) val referralId: UUID? = null,

  @Schema(example = "null", required = true, description = "The identity of the person who added this CourseParticipation")
  @get:JsonProperty("addedBy", required = true) val addedBy: String,

  @Schema(example = "null", required = true, description = "The date and time at which this CourseParticipation was created. ISO 8601 date-time format.")
  @get:JsonProperty("createdAt", required = true) val createdAt: String,

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
