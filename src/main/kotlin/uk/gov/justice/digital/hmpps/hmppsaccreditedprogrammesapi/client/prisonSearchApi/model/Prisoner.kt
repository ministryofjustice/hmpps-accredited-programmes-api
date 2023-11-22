package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model

import java.time.LocalDate

data class Prisoner(
  var prisonerNumber: String? = null,
  var bookingId: String? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var indeterminateSentence: Boolean? = null,
  var nonDtoReleaseDateType: String? = null,
  var conditionalReleaseDate: LocalDate? = null,
  var tariffDate: LocalDate? = null,
  var paroleEligibilityDate: LocalDate? = null,
)
