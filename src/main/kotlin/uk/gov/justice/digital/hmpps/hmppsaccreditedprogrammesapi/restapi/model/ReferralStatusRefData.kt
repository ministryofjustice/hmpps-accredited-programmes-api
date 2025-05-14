package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ReferralStatusRefData(

  @Schema(example = "WITHDRAWN", required = true, description = "A enum-like, computer-friendly string describing the Referral Status")
  @get:JsonProperty("code", required = true) val code: String,

  @Schema(example = "Withdrawn", required = true, description = "A human-readable string describing the Referral Status")
  @get:JsonProperty("description", required = true) val description: String,

  @Schema(example = "light-grey", required = true, description = "A computer-friendly string describing the colour of the referral for display purposes.  This doesn't correspond to CSS default colours, or to hex values.")
  @get:JsonProperty("colour", required = true) val colour: String,

  @Schema(example = "The application has been withdrawn", description = "A human-friendly string that provides information about the status.  These are set by the system, not a human.")
  @get:JsonProperty("hintText") val hintText: String? = null,

  @Schema(example = "I confirm that this person is eligible.", description = "Human-written text to confirm information about the Status", nullable = true)
  @get:JsonProperty("confirmationText") val confirmationText: String? = null,

  @Schema(example = "null", description = "If true, this status should show a notes text box on the UI", nullable = true)
  @get:JsonProperty("hasNotes") val hasNotes: Boolean? = null,

  @Schema(example = "null", description = "If true, this status should show a confirmation box on the UI", nullable = true)
  @get:JsonProperty("hasConfirmation") val hasConfirmation: Boolean? = null,

  @Schema(example = "null", description = "If true, this is a closed status", nullable = true)
  @get:JsonProperty("closed") val closed: Boolean? = null,

  @Schema(example = "null", description = "If true, this is a draft status", nullable = true)
  @get:JsonProperty("draft") val draft: Boolean? = null,

  @Schema(example = "null", description = "If true, this is a hold status", nullable = true)
  @get:JsonProperty("hold") val hold: Boolean? = null,

  @Schema(example = "null", description = "If true, this is a release status", nullable = true)
  @get:JsonProperty("release") val release: Boolean? = null,

  @Schema(example = "null", description = "If true, this a bespoke status of deselected and keep open", nullable = true)
  @get:JsonProperty("deselectAndKeepOpen") val deselectAndKeepOpen: Boolean? = null,

  @Schema(example = "null", description = "Sort order for statuses (presently this seems to be ignored in the UI)", nullable = true)
  @get:JsonProperty("defaultOrder") val defaultOrder: Int? = null,

  @Schema(example = "null", description = "Flag to show whether the notes are optional", nullable = true)
  @get:JsonProperty("notesOptional") val notesOptional: Boolean? = null,
)
