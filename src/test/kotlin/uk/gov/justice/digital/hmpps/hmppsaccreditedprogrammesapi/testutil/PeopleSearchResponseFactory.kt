package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import java.time.LocalDate

class PeopleSearchResponseFactory {
  private var bookingId: String? = null
  private var conditionalReleaseDate: LocalDate? = LocalDate.of(2023, 1, 1)
  private var prisonId: String? = "MDI"
  private var prisonName: String? = "Moorland (HMP & YOI)"
  private var dateOfBirth: LocalDate? = LocalDate.of(1980, 1, 1)
  private var ethnicity: String? = "White"
  private var gender: String? = "Male"
  private var homeDetentionCurfewEligibilityDate: LocalDate? = null
  private var indeterminateSentence: Boolean? = false
  private var firstName: String? = "John"
  private var lastName: String? = "Smith"
  private var paroleEligibilityDate: LocalDate? = LocalDate.of(2023, 1, 1)
  private var prisonerNumber: String = "A1234DD"
  private var religion: String? = null
  private var sentenceExpiryDate: LocalDate? = LocalDate.of(2024, 1, 1)
  private var sentenceStartDate: LocalDate? = LocalDate.of(2022, 1, 1)
  private var tariffDate: LocalDate? = null

  fun withBookingId(bookingId: String?) = apply { this.bookingId = bookingId }
  fun withConditionalReleaseDate(conditionalReleaseDate: LocalDate?) = apply { this.conditionalReleaseDate = conditionalReleaseDate }
  fun withPrisonId(prisonId: String?) = apply { this.prisonId = prisonId }
  fun withPrisonName(prisonName: String?) = apply { this.prisonName = prisonName }
  fun withDateOfBirth(dateOfBirth: LocalDate?) = apply { this.dateOfBirth = dateOfBirth }
  fun withEthnicity(ethnicity: String?) = apply { this.ethnicity = ethnicity }
  fun withGender(gender: String?) = apply { this.gender = gender }
  fun withHomeDetentionCurfewEligibilityDate(homeDetentionCurfewEligibilityDate: LocalDate?) = apply { this.homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate }
  fun withIndeterminateSentence(indeterminateSentence: Boolean?) = apply { this.indeterminateSentence = indeterminateSentence }
  fun withFirstName(firstName: String?) = apply { this.firstName = firstName }
  fun withLastName(lastName: String?) = apply { this.lastName = lastName }
  fun withParoleEligibilityDate(paroleEligibilityDate: LocalDate?) = apply { this.paroleEligibilityDate = paroleEligibilityDate }
  fun withPrisonerNumber(prisonerNumber: String) = apply { this.prisonerNumber = prisonerNumber }
  fun withReligion(religion: String?) = apply { this.religion = religion }
  fun withSentenceExpiryDate(sentenceExpiryDate: LocalDate?) = apply { this.sentenceExpiryDate = sentenceExpiryDate }
  fun withSentenceStartDate(sentenceStartDate: LocalDate?) = apply { this.sentenceStartDate = sentenceStartDate }
  fun withTariffDate(tariffDate: LocalDate?) = apply { this.tariffDate = tariffDate }

  fun produce() = PeopleSearchResponse(
    bookingId = bookingId,
    conditionalReleaseDate = conditionalReleaseDate,
    prisonId = prisonId,
    prisonName = prisonName,
    dateOfBirth = dateOfBirth,
    ethnicity = ethnicity,
    gender = gender,
    homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate,
    indeterminateSentence = indeterminateSentence,
    firstName = firstName,
    lastName = lastName,
    paroleEligibilityDate = paroleEligibilityDate,
    prisonerNumber = prisonerNumber,
    religion = religion,
    sentenceExpiryDate = sentenceExpiryDate,
    sentenceStartDate = sentenceStartDate,
    tariffDate = tariffDate,
  )
}
