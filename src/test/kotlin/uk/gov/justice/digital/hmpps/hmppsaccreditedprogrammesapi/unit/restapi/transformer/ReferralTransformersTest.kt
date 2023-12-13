package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.transformer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.assessmentStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.awaitingAssessment
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralSubmitted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CONDITIONAL_RELEASE_DATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.INDETERMINATE_SENTENCE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.NONDTORELEASE_DATETYPE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PAROLE_ELIGIBILITYDATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_FIRST_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_LAST_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.TARIFF_EXPIRYDATE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import java.util.*
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus as DomainReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate as DomainReferralUpdate

class ReferralTransformersTest {

  private val prisons = mapOf<String?, String>(ORGANISATION_ID to PRISON_NAME)
  private val prisoners = mapOf<String?, List<Prisoner>>(
    uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER to listOf(
      Prisoner(
        prisonerNumber = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER,
        bookingId = BOOKING_ID,
        firstName = PRISONER_FIRST_NAME,
        lastName = PRISONER_LAST_NAME,
        nonDtoReleaseDateType = NONDTORELEASE_DATETYPE,
        conditionalReleaseDate = CONDITIONAL_RELEASE_DATE,
        tariffDate = TARIFF_EXPIRYDATE,
        paroleEligibilityDate = PAROLE_ELIGIBILITYDATE,
        indeterminateSentence = INDETERMINATE_SENTENCE,
      ),
    ),
  )

  @ParameterizedTest
  @EnumSource
  fun `toApi should map status from domain to api`(domainStatus: DomainReferralStatus) {
    val apiStatus = domainStatus.toApi()
    when (domainStatus) {
      REFERRAL_STARTED -> apiStatus shouldBe referralStarted
      REFERRAL_SUBMITTED -> apiStatus shouldBe referralSubmitted
      AWAITING_ASSESSMENT -> apiStatus shouldBe awaitingAssessment
      ASSESSMENT_STARTED -> apiStatus shouldBe assessmentStarted
    }
  }

  @ParameterizedTest
  @EnumSource
  fun `toDomain should map status from api to domain`(apiStatus: ApiReferralStatus) {
    val domainStatus = apiStatus.toDomain()
    when (apiStatus) {
      referralStarted -> domainStatus shouldBe REFERRAL_STARTED
      referralSubmitted -> domainStatus shouldBe REFERRAL_SUBMITTED
      awaitingAssessment -> domainStatus shouldBe AWAITING_ASSESSMENT
      assessmentStarted -> domainStatus shouldBe ASSESSMENT_STARTED
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all fields should convert to its Domain equivalent`() {
    val apiModel = ApiReferralUpdate(
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    with(apiModel.toDomain()) {
      additionalInformation shouldBe apiModel.additionalInformation
      oasysConfirmed shouldBe apiModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe apiModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to Domain`() {
    val apiModel = ApiReferralUpdate(
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
    )

    with(apiModel.toDomain()) {
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all fields should convert to its API equivalent`() {
    val domainModel = DomainReferralUpdate(
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    with(domainModel.toApi()) {
      additionalInformation shouldBe domainModel.additionalInformation
      oasysConfirmed shouldBe domainModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe domainModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to API`() {
    val domainModel = DomainReferralUpdate(
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
    )

    with(domainModel.toApi()) {
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }
}
