package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import java.time.LocalDate

class PrisonerFactory {
  private var prisonerNumber: String = "C6666CC"
  private var bookingId: String? = "1201102"
  private var firstName: String = "JOHN"
  private var lastName: String = "SMITH"
  private var indeterminateSentence: Boolean? = false
  private var nonDtoReleaseDateType: String? = null
  private var conditionalReleaseDate: LocalDate? = null
  private var tariffDate: LocalDate? = null
  private var paroleEligibilityDate: LocalDate? = null
  private var prisonName: String? = "Transfer"
  private var gender: String? = "Male"

  fun withPrisonerNumber(prisonerNumber: String) = apply { this.prisonerNumber = prisonerNumber }
  fun withBookingId(bookingId: String?) = apply { this.bookingId = bookingId }
  fun withFirstName(firstName: String) = apply { this.firstName = firstName }
  fun withLastName(lastName: String) = apply { this.lastName = lastName }
  fun withIndeterminateSentence(indeterminateSentence: Boolean?) = apply { this.indeterminateSentence = indeterminateSentence }
  fun withNonDtoReleaseDateType(nonDtoReleaseDateType: String?) = apply { this.nonDtoReleaseDateType = nonDtoReleaseDateType }
  fun withConditionalReleaseDate(conditionalReleaseDate: LocalDate?) = apply { this.conditionalReleaseDate = conditionalReleaseDate }
  fun withTariffDate(tariffDate: LocalDate?) = apply { this.tariffDate = tariffDate }
  fun withParoleEligibilityDate(paroleEligibilityDate: LocalDate?) = apply { this.paroleEligibilityDate = paroleEligibilityDate }
  fun withPrisonName(prisonName: String?) = apply { this.prisonName = prisonName }
  fun withGender(gender: String?) = apply { this.gender = gender }

  fun produce() = Prisoner(
    prisonerNumber = prisonerNumber,
    bookingId = bookingId,
    firstName = firstName,
    lastName = lastName,
    indeterminateSentence = indeterminateSentence,
    nonDtoReleaseDateType = nonDtoReleaseDateType,
    conditionalReleaseDate = conditionalReleaseDate,
    tariffDate = tariffDate,
    paroleEligibilityDate = paroleEligibilityDate,
    prisonName = prisonName,
    gender = gender,
  )
}
