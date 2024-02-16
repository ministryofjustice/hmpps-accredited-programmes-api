package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import java.time.LocalDate
import java.util.UUID

val COURSE_ID: UUID = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")
val COURSE_NAME: String = "Super course"
val COURSE_LOCATION: String = "West Midlands"
val COURSE_OFFERING_ID: UUID = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")

const val REFERRAL_STARTED = "REFERRAL_STARTED"
const val REFERRAL_STARTED_DESCRIPTION = "Referral Started"
const val REFERRAL_STARTED_COLOUR = "yellow"

const val REFERRAL_SUBMITTED = "REFERRAL_SUBMITTED"
const val REFERRAL_SUBMITTED_DESCRIPTION = "Referral Submitted"
const val REFERRAL_SUBMITTED_COLOUR = "light-pink"

const val PRISON_NUMBER_1 = "C6666CC"
const val REFERRER_USERNAME = "TEST_REFERRER_USER_1"
const val ORGANISATION_ID_MDI = "MDI"
const val BOOKING_ID = "Booking id"
const val INDETERMINATE_SENTENCE = false
const val NON_DTO_RELEASE_DATE_TYPE = "Release date type"
val CONDITIONAL_RELEASE_DATE: LocalDate = LocalDate.now()
val TARIFF_EXPIRY_DATE: LocalDate = LocalDate.now().minusDays(5)
val PAROLE_ELIGIBILITY_DATE: LocalDate = LocalDate.now().plusYears(1)

const val PRISON_ID_1 = "1"
const val PRISON_NAME_1 = "PRISON_ONE"

val PRISONER_1 = Prisoner(
  prisonerNumber = PRISON_NUMBER_1,
  bookingId = BOOKING_ID,
  firstName = "JOHN",
  lastName = "SMITH",
  nonDtoReleaseDateType = NON_DTO_RELEASE_DATE_TYPE,
  conditionalReleaseDate = CONDITIONAL_RELEASE_DATE,
  tariffDate = TARIFF_EXPIRY_DATE,
  paroleEligibilityDate = PAROLE_ELIGIBILITY_DATE,
  indeterminateSentence = INDETERMINATE_SENTENCE,
)

const val PRISON_LOCATION = "Buckinghamshire"
