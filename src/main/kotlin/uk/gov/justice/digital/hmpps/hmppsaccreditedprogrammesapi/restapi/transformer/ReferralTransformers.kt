package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate

fun ReferralEntity.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offeringId,
  prisonNumber = prisonNumber,
  referrerId = referrerId,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  reason = reason,
  additionalInformation = additionalInformation,
  status = status.toApi(),
)

fun ReferralStatus.toApi(): ApiReferralStatus = when (this) {
  ReferralStatus.ASSESSMENT_STARTED -> ApiReferralStatus.assessmentStarted
  ReferralStatus.REFERRAL_STARTED -> ApiReferralStatus.referralStarted
  ReferralStatus.REFERRAL_SUBMITTED -> ApiReferralStatus.referralSubmitted
  ReferralStatus.AWAITING_ASSESSMENT -> ApiReferralStatus.awaitingAssessment
}

fun ApiReferralStatus.toDomain(): ReferralStatus = when (this) {
  ApiReferralStatus.referralStarted -> ReferralStatus.REFERRAL_STARTED
  ApiReferralStatus.referralSubmitted -> ReferralStatus.REFERRAL_SUBMITTED
  ApiReferralStatus.awaitingAssessment -> ReferralStatus.AWAITING_ASSESSMENT
  ApiReferralStatus.assessmentStarted -> ReferralStatus.ASSESSMENT_STARTED
}

fun ApiReferralUpdate.toDomain() = ReferralUpdate(
  reason = reason,
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
)

fun ReferralUpdate.toApi() = ApiReferralUpdate(
  reason = reason,
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
)
