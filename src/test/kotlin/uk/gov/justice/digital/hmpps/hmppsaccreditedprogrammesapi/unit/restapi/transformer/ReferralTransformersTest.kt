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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus as DomainReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate as DomainReferralUpdate

class ReferralTransformersTest {
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
      reason = "Some reason",
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true
    )

    with(apiModel.toDomain()) {
      reason shouldBe apiModel.reason
      additionalInformation shouldBe apiModel.additionalInformation
      oasysConfirmed shouldBe apiModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe apiModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to Domain`() {
    val apiModel = ApiReferralUpdate(
      reason = null,
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false
    )

    with(apiModel.toDomain()) {
      reason shouldBe null
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all fields should convert to its API equivalent`() {
    val domainModel = DomainReferralUpdate(
      reason = "Some reason",
      additionalInformation = "Additional Info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true
    )

    with(domainModel.toApi()) {
      reason shouldBe domainModel.reason
      additionalInformation shouldBe domainModel.additionalInformation
      oasysConfirmed shouldBe domainModel.oasysConfirmed
      hasReviewedProgrammeHistory shouldBe domainModel.hasReviewedProgrammeHistory
    }
  }

  @Test
  fun `Transforming a ReferralUpdate with all nullable fields should tolerantly convert to API`() {
    val domainModel = DomainReferralUpdate(
      reason = null,
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false
    )

    with(domainModel.toApi()) {
      reason shouldBe null
      additionalInformation shouldBe null
      oasysConfirmed shouldBe false
      hasReviewedProgrammeHistory shouldBe false
    }
  }

}
