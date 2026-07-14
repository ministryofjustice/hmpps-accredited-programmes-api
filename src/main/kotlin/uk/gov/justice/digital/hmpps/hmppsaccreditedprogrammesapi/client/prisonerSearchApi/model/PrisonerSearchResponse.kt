package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonerSearchResponse(
  val bookingId: String,
  val conditionalReleaseDate: LocalDate?,
  val prisonName: String?,
  val dateOfBirth: LocalDate?,
  val ethnicity: String?,
  val gender: String?,
  val homeDetentionCurfewEligibilityDate: LocalDate?,
  val indeterminateSentence: Boolean?,
  val firstName: String?,
  val lastName: String?,
  val paroleEligibilityDate: LocalDate?,
  val prisonerNumber: String,
  val religion: String?,
  val sentenceExpiryDate: LocalDate?,
  val sentenceStartDate: LocalDate?,
  val tariffDate: LocalDate?,
)
