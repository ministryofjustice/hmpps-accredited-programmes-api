package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param prisonNumber The prison number of the course participant.
 * @param courseName The name of the course taken by the participant.
 * @param setting
 * @param outcome
 * @param detail
 * @param source
 */
data class CourseParticipationCreate(

  @Schema(example = "A1234AA", required = true, description = "The prison number of the course participant.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

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

   @Schema(example = "null", description = "The unique id (UUID) of the associated referral.")
  @get:JsonProperty("referralId") val referralId: UUID? = null,

  @Schema(example = "null", description = "Whether this is a draft record or not.")
  @get:JsonProperty("isDraft") val isDraft: Boolean? = false,
  )
