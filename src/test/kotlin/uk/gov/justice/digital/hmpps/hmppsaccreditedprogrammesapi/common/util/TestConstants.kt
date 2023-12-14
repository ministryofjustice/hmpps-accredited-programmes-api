package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import java.time.LocalDate
import java.util.UUID

val COURSE_ID: UUID = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")
val COURSE_OFFERING_ID: UUID = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")
const val PRISON_NUMBER = "A1234AA"
const val PRISON_NAME = "Moorland"
const val CLIENT_USERNAME = "TEST_REFERRER_USER_1"
const val PRISONER_FIRST_NAME = "John"
const val PRISONER_LAST_NAME = "Doe"
const val ORGANISATION_ID_MDI = "MDI"
const val BOOKING_ID = "Booking id"
const val INDETERMINATE_SENTENCE = false
const val NON_DTO_RELEASE_DATE_TYPE = "Release date type"
const val REFERRER_USERNAME = "autobot"
const val INDIGO_COURSE = "Indigo Course"
const val WHITE_COURSE = "White Course"
const val SEXUAL_OFFENCE = "Sexual offence"
const val EXTREMISM_OFFENCE = "Extremism offence"
val CONDITIONAL_RELEASE_DATE: LocalDate = LocalDate.now()
val TARIFF_EXPIRY_DATE: LocalDate = LocalDate.now().minusDays(5)
val PAROLE_ELIGIBILITY_DATE: LocalDate = LocalDate.now().plusYears(1)
val PRISONS = mapOf<String?, String>(ORGANISATION_ID_MDI to PRISON_NAME)
val PRISONERS = mapOf<String?, List<Prisoner>>(
  PRISON_NUMBER to listOf(
    Prisoner(
      prisonerNumber = PRISON_NUMBER,
      bookingId = BOOKING_ID,
      firstName = PRISONER_FIRST_NAME,
      lastName = PRISONER_LAST_NAME,
      nonDtoReleaseDateType = NON_DTO_RELEASE_DATE_TYPE,
      conditionalReleaseDate = CONDITIONAL_RELEASE_DATE,
      tariffDate = TARIFF_EXPIRY_DATE,
      paroleEligibilityDate = PAROLE_ELIGIBILITY_DATE,
      indeterminateSentence = INDETERMINATE_SENTENCE,
    ),
  ),
)
const val PRISON_ID_1 = "1"
const val PRISON_ID_2 = "2"
const val PRISON_NUMBER_1 = "001"
const val PRISON_NUMBER_2 = "002"
const val PRISON_NAME_1 = "PRISON_ONE"
const val PRISON_NAME_2 = "PRISON_TWO"
val PRISONER_1 = Prisoner(prisonerNumber = PRISON_NUMBER_1, firstName = "John", lastName = "Doe")
val PRISONER_2 = Prisoner(prisonerNumber = PRISON_NUMBER_2, firstName = "Ella", lastName = "Smith")
