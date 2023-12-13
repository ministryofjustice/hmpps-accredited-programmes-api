package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate
fun ReferralEntity.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offering.id!!,
  prisonNumber = prisonNumber,
  referrerUsername = referrer.username,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  additionalInformation = additionalInformation,
  status = status.toApi(),
  submittedOn = submittedOn?.toString(),
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
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
)

fun ReferralUpdate.toApi() = ApiReferralUpdate(
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
)
