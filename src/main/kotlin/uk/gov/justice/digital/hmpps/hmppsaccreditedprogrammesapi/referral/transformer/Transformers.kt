package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral as DomainReferral

fun DomainReferral.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offeringId,
  prisonNumber = prisonNumber,
  referrerId = referrerId,
  oasysConfirmed = oasysConfirmed,
  reason = reason,
  status = status.toApi(),
)

fun DomainReferral.Status.toApi(): ApiReferralStatus = when (this) {
  DomainReferral.Status.ASSESSMENT_STARTED -> ApiReferralStatus.assessmentStarted
  DomainReferral.Status.REFERRAL_STARTED -> ApiReferralStatus.referralStarted
  DomainReferral.Status.REFERRAL_SUBMITTED -> ApiReferralStatus.referralSubmitted
  DomainReferral.Status.AWAITING_ASSESSMENT -> ApiReferralStatus.awaitingAssessment
}
