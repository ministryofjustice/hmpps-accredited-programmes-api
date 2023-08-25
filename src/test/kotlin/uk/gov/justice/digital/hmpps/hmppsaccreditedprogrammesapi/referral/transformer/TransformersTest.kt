package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.transformer

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status as DomainReferralStatus

class TransformersTest {
  @ParameterizedTest()
  @EnumSource
  fun `maps status from domain to api`(domainStatus: DomainReferralStatus) {
    val apiStatus = domainStatus.toApi()
    when (domainStatus) {
      DomainReferralStatus.REFERRAL_STARTED -> apiStatus shouldBe ApiReferralStatus.referralStarted
      DomainReferralStatus.REFERRAL_SUBMITTED -> apiStatus shouldBe ApiReferralStatus.referralSubmitted
      DomainReferralStatus.AWAITING_ASSESSMENT -> apiStatus shouldBe ApiReferralStatus.awaitingAssessment
      DomainReferralStatus.ASSESSMENT_STARTED -> apiStatus shouldBe ApiReferralStatus.assessmentStarted
    }
  }
}
