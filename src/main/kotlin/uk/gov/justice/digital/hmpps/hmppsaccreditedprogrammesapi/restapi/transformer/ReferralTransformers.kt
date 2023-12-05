package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Name
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus as ApiReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate

fun ReferralEntity.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offering.id!!,
  prisonNumber = prisonNumber,
  referrerUsername = referrer.username,
  referrerId = referrerId,
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

fun List<ReferralSummaryProjection>.toApi(
  prisonersDetails: Map<String?, List<Prisoner>>,
  allPrisonDetails: Map<String?, String?>,
  organisationId: String,
): List<ReferralSummary> {
  return this.groupBy { it.referralId }
    .map { (id, projections) ->
      val firstProjection = projections.first()

      ReferralSummary(
        id = id,
        referrerUsername = firstProjection.referrerUsername,
        courseName = firstProjection.courseName,
        audiences = projections.map { it.audience }.distinct(),
        status = firstProjection.status.toApi(),
        submittedOn = firstProjection.submittedOn?.toString(),
        prisonNumber = firstProjection.prisonNumber,
        organisationId = organisationId,
        prisonName = allPrisonDetails[organisationId].orEmpty(),
        prisonerName = getPrisonerName(firstProjection, prisonersDetails),
        sentenceDate = getSentenceDate(firstProjection, prisonersDetails),
      )
    }
}

private fun getSentenceDate(referralSummaryProjection: ReferralSummaryProjection, prisonersDetails: Map<String?, List<Prisoner>>): SentenceDate? {
  return prisonersDetails[referralSummaryProjection.prisonNumber]?.getOrNull(0)?.let {
    SentenceDate(
      conditionalReleaseDate = it.conditionalReleaseDate,
      tariffExpiryDate = it.tariffDate,
      paroleEligibilityDate = it.paroleEligibilityDate,
      indeterminateSentence = it.indeterminateSentence,
      nonDtoReleaseDateType = it.nonDtoReleaseDateType,
    )
  }
}

private fun getPrisonerName(referralSummaryProjection: ReferralSummaryProjection, prisonersDetails: Map<String?, List<Prisoner>>): Name? {
  return prisonersDetails[referralSummaryProjection.prisonNumber]?.getOrNull(0)?.let {
    Name(
      firstName = it.firstName,
      lastName = it.lastName,
    )
  }
}
