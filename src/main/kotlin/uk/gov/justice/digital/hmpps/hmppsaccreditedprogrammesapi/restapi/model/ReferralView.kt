package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param id The unique id (UUID) of the new referral.
 * @param referrerUsername The unique HMPPS username of the user who created this referral.
 * @param courseName
 * @param audience
 * @param status
 * @param statusDescription The status description.
 * @param statusColour The colour to display status description.
 * @param submittedOn Date referral was submitted.
 * @param prisonNumber
 * @param organisationName Name of the organisation
 * @param organisationId ID of the organisation
 * @param conditionalReleaseDate Conditional release date.
 * @param paroleEligibilityDate Parole eligibility date.
 * @param tariffExpiryDate Tariff expiry date.
 * @param earliestReleaseDate Earliest release date, if applicable, to this individual. Derived from Sentence information.
 * @param earliestReleaseDateType Earliest release date type used
 * @param nonDtoReleaseDateType Release date type
 * @param forename forename of the person
 * @param surname surname of the person
 * @param sentenceType Sentence type description or 'Multiple sentences' if there are more than one
 * @param listDisplayName The course display name when it is in a list.
 * @param location location of person
 */
data class ReferralView(

  @Schema(example = "null", description = "The unique id (UUID) of the new referral.")
  @get:JsonProperty("id") val id: UUID? = null,

  @Schema(example = "null", description = "The unique HMPPS username of the user who created this referral.")
  @get:JsonProperty("referrerUsername") val referrerUsername: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("courseName") val courseName: String? = null,

  @Schema(example = "Gang offence", description = "")
  @get:JsonProperty("audience") val audience: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("status") val status: String? = null,

  @Schema(example = "null", description = "The status description.")
  @get:JsonProperty("statusDescription") val statusDescription: String? = null,

  @Schema(example = "null", description = "The colour to display status description.")
  @get:JsonProperty("statusColour") val statusColour: String? = null,

  @Schema(example = "null", description = "Date referral was submitted.")
  @get:JsonProperty("submittedOn") val submittedOn: java.time.Instant? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("prisonNumber") val prisonNumber: String? = null,

  @Schema(example = "null", description = "Name of the organisation")
  @get:JsonProperty("organisationName") val organisationName: String? = null,

  @Schema(example = "null", description = "ID of the organisation")
  @get:JsonProperty("organisationId") val organisationId: String? = null,

  @Schema(example = "null", description = "Conditional release date.")
  @get:JsonProperty("conditionalReleaseDate") val conditionalReleaseDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "Parole eligibility date.")
  @get:JsonProperty("paroleEligibilityDate") val paroleEligibilityDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "Tariff expiry date.")
  @get:JsonProperty("tariffExpiryDate") val tariffExpiryDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "Earliest release date, if applicable, to this individual. Derived from Sentence information.")
  @get:JsonProperty("earliestReleaseDate") val earliestReleaseDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "Earliest release date type used")
  @get:JsonProperty("earliestReleaseDateType") val earliestReleaseDateType: String? = null,

  @Schema(example = "null", description = "Release date type")
  @get:JsonProperty("nonDtoReleaseDateType") val nonDtoReleaseDateType: String? = null,

  @Schema(example = "null", description = "forename of the person")
  @get:JsonProperty("forename") val forename: String? = null,

  @Schema(example = "null", description = "surname of the person")
  @get:JsonProperty("surname") val surname: String? = null,

  @Schema(example = "null", description = "Sentence type description or 'Multiple sentences' if there are more than one")
  @get:JsonProperty("sentenceType") val sentenceType: String? = null,

  @Schema(example = "null", description = "The course display name when it is in a list.")
  @get:JsonProperty("listDisplayName") val listDisplayName: String? = null,

  @Schema(example = "null", description = "location of person")
  @get:JsonProperty("location") val location: String? = null,

  @Schema(example = "true", description = "Flag to indicate learning difficulties and challenges")
  @get:JsonProperty("hasLdc") val hasLdc: Boolean? = null,
)
