package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param code
 * @param description
 * @param colour
 * @param hintText
 * @param confirmationText
 * @param hasNotes flag to show this status has notes text box
 * @param hasConfirmation flag to show this status has confirmation box
 * @param closed flag to show this is a closed status
 * @param draft flag to show this is a draft status
 * @param hold flag to show this is a hold status
 * @param release flag to show this is a release status
 * @param deselectAndKeepOpen flag to show this a bespoke status of deslected and keep open
 * @param defaultOrder sort order for statuses
 * @param notesOptional flag to show whether the notes are optional
 */
data class ReferralStatusRefData(

  @Schema(example = "WITHDRAWN", required = true, description = "")
  @get:JsonProperty("code", required = true) val code: String,

  @Schema(example = "Withdrawn", required = true, description = "")
  @get:JsonProperty("description", required = true) val description: String,

  @Schema(example = "light-grey", required = true, description = "")
  @get:JsonProperty("colour", required = true) val colour: String,

  @Schema(example = "The application has been withdrawn", description = "")
  @get:JsonProperty("hintText") val hintText: String? = null,

  @Schema(example = "I confirm that this person is eligible.", description = "")
  @get:JsonProperty("confirmationText") val confirmationText: String? = null,

  @Schema(example = "null", description = "flag to show this status has notes text box")
  @get:JsonProperty("hasNotes") val hasNotes: Boolean? = null,

  @Schema(example = "null", description = "flag to show this status has confirmation box")
  @get:JsonProperty("hasConfirmation") val hasConfirmation: Boolean? = null,

  @Schema(example = "null", description = "flag to show this is a closed status")
  @get:JsonProperty("closed") val closed: Boolean? = null,

  @Schema(example = "null", description = "flag to show this is a draft status")
  @get:JsonProperty("draft") val draft: Boolean? = null,

  @Schema(example = "null", description = "flag to show this is a hold status")
  @get:JsonProperty("hold") val hold: Boolean? = null,

  @Schema(example = "null", description = "flag to show this is a release status")
  @get:JsonProperty("release") val release: Boolean? = null,

  @Schema(example = "null", description = "flag to show this a bespoke status of deslected and keep open")
  @get:JsonProperty("deselectAndKeepOpen") val deselectAndKeepOpen: Boolean? = null,

  @Schema(example = "null", description = "sort order for statuses")
  @get:JsonProperty("defaultOrder") val defaultOrder: Int? = null,

  @Schema(example = "null", description = "flag to show whether the notes are optional")
  @get:JsonProperty("notesOptional") val notesOptional: Boolean? = null,
)
