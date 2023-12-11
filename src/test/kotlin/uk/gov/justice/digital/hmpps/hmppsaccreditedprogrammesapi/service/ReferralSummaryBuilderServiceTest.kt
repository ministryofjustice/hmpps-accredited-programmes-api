package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CONDITIONAL_RELEASE_DATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.EXTREMISM_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.INDETERMINATE_SENTENCE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.INDIGO_COURSE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.NON_DTO_RELEASE_DATE_TYPE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PAROLE_ELIGIBILITY_DATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONERS
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_FIRST_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_LAST_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.SEXUAL_OFFENCE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.TARIFF_EXPIRY_DATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.WHITE_COURSE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import java.time.LocalDateTime

class ReferralSummaryBuilderServiceTest {

  val service = ReferralSummaryBuilderService()
  val uuid1 = java.util.UUID.randomUUID()
  val uuid2 = java.util.UUID.randomUUID()

  companion object {
    private val referralSummaryProjection1 = ReferralSummaryProjection(
      referralId = UUID.randomUUID(),
      courseName = INDIGO_COURSE,
      audience = SEXUAL_OFFENCE,
      status = ReferralEntity.ReferralStatus.ASSESSMENT_STARTED,
      submittedOn = LocalDateTime.now(),
      prisonNumber = PRISON_NUMBER,
      referrerUsername = REFERRER_USERNAME,
    )

  private val referralSummaryProjection1 = ReferralSummaryProjection(
    referralId = uuid1,
    courseName = INDIGO_COURSE,
    audience = SEXUAL_OFFENCE,
    status = ReferralEntity.ReferralStatus.ASSESSMENT_STARTED,
    submittedOn = LocalDateTime.now(),
    prisonNumber = PRISON_NUMBER,
    referrerUsername = REFERRER_USERNAME,
  )

  private val referralSummaryProjection2 = ReferralSummaryProjection(
    referralId = uuid2,
    courseName = WHITE_COURSE,
    audience = EXTREMISM_OFFENCE,
    status = ReferralEntity.ReferralStatus.ASSESSMENT_STARTED,
    submittedOn = LocalDateTime.now(),
    prisonNumber = PRISON_NUMBER,
    referrerUsername = REFERRER_USERNAME,
  )

  @Test
  fun `build referral summary successful`() {
    val referralSummaries =
      service.build(listOf(referralSummaryProjection1, referralSummaryProjection2), prisoners, prisons, ORGANISATION_ID)

    assertEquals(2, referralSummaries.size)

    with(referralSummaries[0]) {
      organisationId shouldBe ORGANISATION_ID
      id shouldBe uuid1
      courseName shouldBe INDIGO_COURSE
      prisonNumber shouldBe PRISON_NUMBER
      prisonName shouldBe PRISON_NAME
      prisonerName?.firstName shouldBe PRISONER_FIRST_NAME
      prisonerName?.lastName shouldBe PRISONER_LAST_NAME
      sentence?.indeterminateSentence shouldBe INDETERMINATE_SENTENCE
      sentence?.conditionalReleaseDate shouldBe CONDITIONAL_RELEASE_DATE
      sentence?.nonDtoReleaseDateType shouldBe NON_DTO_RELEASE_DATE_TYPE
      sentence?.tariffExpiryDate shouldBe TARIFF_EXPIRY_DATE
      sentence?.paroleEligibilityDate shouldBe PAROLE_ELIGIBILITY_DATE
    }

    with(referralSummaries[1]) {
      organisationId shouldBe ORGANISATION_ID
      id shouldBe uuid2
      courseName shouldBe WHITE_COURSE
      prisonNumber shouldBe PRISON_NUMBER
      prisonName shouldBe PRISON_NAME
      prisonerName?.firstName shouldBe PRISONER_FIRST_NAME
      prisonerName?.lastName shouldBe PRISONER_LAST_NAME
      sentence?.indeterminateSentence shouldBe INDETERMINATE_SENTENCE
      sentence?.conditionalReleaseDate shouldBe CONDITIONAL_RELEASE_DATE
      sentence?.nonDtoReleaseDateType shouldBe NON_DTO_RELEASE_DATE_TYPE
      sentence?.tariffExpiryDate shouldBe TARIFF_EXPIRY_DATE
      sentence?.paroleEligibilityDate shouldBe PAROLE_ELIGIBILITY_DATE
    }
  }
}
