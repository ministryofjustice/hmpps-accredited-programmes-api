package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.transformer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.assessmentStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.awaitingAssessment
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralSubmitted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralSummaryProjectionFactory
import java.util.*
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus as DomainReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate as DomainReferralUpdate

class ReferralTransformersTest {

  companion object {
    const val PRISON_NUMBER = "A1234AA"
  }

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

  @Test
  fun `Transforming a ReferralSummary with all fields should convert to its API equivalent`() {
    val referralId = UUID.randomUUID()
    val collatedAudiences = listOf("Audience 1", "Audience 2", "Audience 3")
    val referralSummaryProjections = collatedAudiences.map { audience ->
      ReferralSummaryProjectionFactory()
        .withReferralId(referralId)
        .withCourseName("Course name")
        .withAudience(audience)
        .withStatus(REFERRAL_STARTED)
        .withPrisonNumber(PRISON_NUMBER)
        .produce()
    }

    with(referralSummaryProjections.toApi().first()) {
      id shouldBe referralId
      courseName shouldBe "Course name"
      audiences shouldBe collatedAudiences
      status shouldBe referralStarted
      prisonNumber shouldBe PRISON_NUMBER
    }
  }
}
