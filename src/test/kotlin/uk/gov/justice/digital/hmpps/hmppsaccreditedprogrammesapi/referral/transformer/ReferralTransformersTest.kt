package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.transformer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.assessmentStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.awaitingAssessment
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus.referralSubmitted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status as DomainReferralStatus

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
}
