package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral as DomainReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status as DomainReferralStatus

fun DomainReferral.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offeringId,
  prisonNumber = prisonNumber,
  referrerId = referrerId,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  reason = reason,
  status = status.toApi(),
)

fun DomainReferral.Status.toApi(): ApiReferralStatus = when (this) {
  DomainReferralStatus.ASSESSMENT_STARTED -> ApiReferralStatus.assessmentStarted
  DomainReferralStatus.REFERRAL_STARTED -> ApiReferralStatus.referralStarted
  DomainReferralStatus.REFERRAL_SUBMITTED -> ApiReferralStatus.referralSubmitted
  DomainReferralStatus.AWAITING_ASSESSMENT -> ApiReferralStatus.awaitingAssessment
}

fun ApiReferralStatus.toDomain(): DomainReferralStatus = when (this) {
  ApiReferralStatus.referralStarted -> DomainReferralStatus.REFERRAL_STARTED
  ApiReferralStatus.referralSubmitted -> DomainReferralStatus.REFERRAL_SUBMITTED
  ApiReferralStatus.awaitingAssessment -> DomainReferralStatus.AWAITING_ASSESSMENT
  ApiReferralStatus.assessmentStarted -> DomainReferralStatus.ASSESSMENT_STARTED
}
