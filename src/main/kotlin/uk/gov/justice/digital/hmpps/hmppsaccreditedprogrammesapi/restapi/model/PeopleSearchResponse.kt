package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class PeopleSearchResponse(

  @Schema(example = "1202156", description = "The NOMIS booking ID")
  val bookingId: String? = null,

  @Schema(example = "A9999DY", description = "The NOMIS prisoner number")
  val prisonerNumber: String? = null,

  @Schema(example = "2027-08-25", description = "The conditional release date")
  val conditionalReleaseDate: LocalDate? = null,

  @Schema(example = "MDI", description = "ID of the prison")
  val prisonId: String? = null,

  @Schema(example = "HMP Leeds", description = "The name of the prison")
  val prisonName: String? = null,

  @Schema(example = "1986-06-23", description = "The prisoner's date of birth")
  val dateOfBirth: LocalDate? = null,

  @Schema(example = "White", description = "The prisoner's ethnicity")
  val ethnicity: String? = null,

  @Schema(example = "Female", description = "The prisoner's gender")
  val gender: String? = null,

  @Schema(example = "2027-08-25", description = "The prisoner's home detention curfew eligibility date")
  val homeDetentionCurfewEligibilityDate: LocalDate? = null,

  @Schema(example = "false", description = "A boolean denoting whether the prisoner has an indeterminate sentence")
  val indeterminateSentence: Boolean? = null,

  @Schema(example = "John", description = "The prisoner's first name")
  val firstName: String? = null,

  @Schema(example = "Doe", description = "The prisoner's last name")
  val lastName: String? = null,

  @Schema(example = "2027-08-25", description = "The prisoner's parole eligibility date")
  val paroleEligibilityDate: LocalDate? = null,

  @Schema(example = "Christian", description = "The prisoner's religion")
  val religion: String? = null,

  @Schema(example = "2027-08-25", description = "The prisoner's sentence expiry date")
  val sentenceExpiryDate: LocalDate? = null,

  @Schema(example = "2027-08-25", description = "The prisoner's sentence start date")
  val sentenceStartDate: LocalDate? = null,

  @Schema(example = "2027-08-25", description = "The prisoner's tariff date")
  val tariffDate: LocalDate? = null,
)
