package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param primaryHeading
 * @param primaryDescription
 * @param secondaryHeading
 * @param secondaryDescription
 * @param warningText
 * @param hasConfirmation
 * @param notesOptional
 */
data class ConfirmationFields(

  @Schema(example = "Move referral to awaiting assessment", description = "")
  @get:JsonProperty("primaryHeading") val primaryHeading: kotlin.String? = null,

  @Schema(example = "Submitting this will change the status to awaiting assessment.", description = "")
  @get:JsonProperty("primaryDescription") val primaryDescription: kotlin.String? = null,

  @Schema(example = "Give a reason", description = "")
  @get:JsonProperty("secondaryHeading") val secondaryHeading: kotlin.String? = null,

  @Schema(example = "You must give a reason why this referral is being moved to suitable but not ready.", description = "")
  @get:JsonProperty("secondaryDescription") val secondaryDescription: kotlin.String? = null,

  @Schema(example = "Submitting this will pause the referral", description = "")
  @get:JsonProperty("warningText") val warningText: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("hasConfirmation") val hasConfirmation: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("notesOptional") val notesOptional: kotlin.Boolean? = false,
)
