package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util

import java.time.LocalDate
import java.util.UUID

const val PRISON_NUMBER = "A1234AA"
const val PRISON_NAME = "Moorland"
const val REFERRER_ID = "MWX001"
const val CLIENT_USERNAME = "TEST_REFERRER_USER_1"
val COURSE_ID: UUID = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")
val COURSE_OFFERING_ID: UUID = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")
const val PRISONER_FIRST_NAME = "John"
const val PRISONER_LAST_NAME = "Doe"
const val ORGANISATION_ID = "MDI"
val BOOKING_ID = "Booking id"
const val INDETERMINATE_SENTENCE = false
const val NONDTORELEASE_DATETYPE = "Release date type"
val CONDITIONAL_RELEASE_DATE = LocalDate.now()
val TARIFF_EXPIRYDATE = LocalDate.now().minusDays(5)

val PAROLE_ELIGIBILITYDATE = LocalDate.now().plusYears(1)
val REFERRER_USERNAME = "autobot"

val INDIGO_COURSE = "Indigo Course"

val SEXUAL_OFFENCE = "Sexual offence"

val WHITE_COURSE = "White Course"

val EXTREMISM_OFFENCE = "Extremism offence"
