package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.StaffDetail as PrisonOffenderManager

/**
 *
 * @param offeringId The id (UUID) of an active offering
 * @param prisonNumber The prison number of the person who is being referred.
 * @param oasysConfirmed
 * @param hasReviewedProgrammeHistory
 * @param id The unique id (UUID) of this referral.
 * @param status The status code of the referral.
 * @param referrerUsername
 * @param additionalInformation
 * @param closed Is the status of the referral a closed one.
 * @param statusDescription The status description.
 * @param statusColour The colour to display status description.
 * @param submittedOn
 * @param overrideReason
 * @param transferReason
 * @param originalReferralId
 */
data class Referral(

  @Schema(example = "null", required = true, description = "The id (UUID) of an active offering")
  @get:JsonProperty("offeringId", required = true) val offeringId: UUID,

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("oasysConfirmed", required = true) val oasysConfirmed: Boolean = false,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("hasReviewedProgrammeHistory", required = true) val hasReviewedProgrammeHistory: Boolean = false,

  @Schema(example = "null", required = true, description = "The unique id (UUID) of this referral.")
  @get:JsonProperty("id", required = true) val id: UUID,

  @Schema(example = "null", required = true, description = "The status code of the referral.")
  @get:JsonProperty("status", required = true) val status: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("referrerUsername", required = true) val referrerUsername: String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("additionalInformation") val additionalInformation: String? = null,

  @Schema(example = "null", description = "Is the status of the referral a closed one.")
  @get:JsonProperty("closed") val closed: Boolean? = null,

  @Schema(example = "null", description = "The status description.")
  @get:JsonProperty("statusDescription") val statusDescription: String? = null,

  @Schema(example = "null", description = "The colour to display status description.")
  @get:JsonProperty("statusColour") val statusColour: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("submittedOn") val submittedOn: String? = null,

  @Schema(example = "The reason for going with the recommended course is...", description = "Reason for overriding the recommended course")
  @get:JsonProperty("overrideReason") val overrideReason: String? = null,

  @Schema(example = "44e3cdab-c996-4234-afe5-a9d8ddb13be8", description = "Referral ID of the original referral from which transfer was initiated")
  @get:JsonProperty("originalReferralId") val originalReferralId: UUID? = null,

  @Schema(example = "true", description = "Flag to indicate learning difficulties and challenges")
  @get:JsonProperty("hasLdc") val hasLdc: Boolean? = null,

  @Schema(example = "true", description = "Flag to indicate if the ldc field was overridden by the programme team")
  @get:JsonProperty("hasLdcBeenOverriddenByProgrammeTeam") val hasLdcBeenOverriddenByProgrammeTeam: Boolean = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("primaryPrisonOffenderManager") val primaryPrisonOffenderManager: PrisonOffenderManager? = null,
)
