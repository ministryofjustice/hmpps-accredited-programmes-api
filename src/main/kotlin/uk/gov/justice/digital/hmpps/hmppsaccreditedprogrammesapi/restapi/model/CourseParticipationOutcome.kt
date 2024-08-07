package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * The outcome of participating in a course.
 * @param status
 * @param yearStarted
 * @param yearCompleted
 */
data class CourseParticipationOutcome(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("status", required = true) val status: CourseParticipationOutcome.Status,

  @Schema(example = "null", description = "")
  @get:JsonProperty("yearStarted") val yearStarted: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("yearCompleted") val yearCompleted: kotlin.Int? = null,
) {

  /**
   *
   * Values: incomplete,complete
   */
  enum class Status(val value: kotlin.String) {

    @JsonProperty("incomplete")
    INCOMPLETE("incomplete"),

    @JsonProperty("complete")
    COMPLETE("complete"),
  }
}
