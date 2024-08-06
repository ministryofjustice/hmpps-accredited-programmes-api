package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param bookingId
 * @param prisonerNumber
 * @param conditionalReleaseDate
 * @param prisonName
 * @param dateOfBirth
 * @param ethnicity
 * @param gender
 * @param homeDetentionCurfewEligibilityDate
 * @param indeterminateSentence
 * @param firstName
 * @param lastName
 * @param paroleEligibilityDate
 * @param religion
 * @param sentenceExpiryDate
 * @param sentenceStartDate
 * @param tariffDate
 */
data class PrisonerSearchResponse(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("bookingId", required = true) val bookingId: kotlin.String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("prisonerNumber", required = true) val prisonerNumber: kotlin.String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("conditionalReleaseDate") val conditionalReleaseDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("prisonName") val prisonName: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("dateOfBirth") val dateOfBirth: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("ethnicity") val ethnicity: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("gender") val gender: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("homeDetentionCurfewEligibilityDate") val homeDetentionCurfewEligibilityDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("indeterminateSentence") val indeterminateSentence: kotlin.Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("firstName") val firstName: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("lastName") val lastName: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("paroleEligibilityDate") val paroleEligibilityDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("religion") val religion: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("sentenceExpiryDate") val sentenceExpiryDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("sentenceStartDate") val sentenceStartDate: java.time.LocalDate? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("tariffDate") val tariffDate: java.time.LocalDate? = null,
)
