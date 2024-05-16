package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi.model.LinkedScheduleDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewEntity
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate as ApiReferralUpdate

fun ReferralEntity.toApi(status: ReferralStatusRefData): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offering.id!!,
  prisonNumber = prisonNumber,
  referrerUsername = referrer.username,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  additionalInformation = additionalInformation,
  status = status.code.lowercase(),
  closed = status.closed,
  statusDescription = status.description,
  statusColour = status.colour,
  submittedOn = submittedOn?.toString(),
)

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

fun ReferralViewEntity.toApi() = ReferralView(
  id = id,
  referrerUsername = referrerUsername,
  courseName = courseName,
  audience = audience,
  status = status?.lowercase(),
  statusDescription = statusDescription,
  statusColour = statusColour,
  submittedOn = submittedOn?.toInstant(ZoneOffset.UTC),
  prisonNumber = prisonNumber,
  organisationName = organisationName,
  organisationId = organisationId,
  conditionalReleaseDate = conditionalReleaseDate,
  paroleEligibilityDate = paroleEligibilityDate,
  tariffExpiryDate = tariffExpiryDate,
  earliestReleaseDate = earliestReleaseDate,
  earliestReleaseDateType = earliestReleaseDateType,
  nonDtoReleaseDateType = nonDtoReleaseDateType,
  forename = forename,
  surname = surname,
  sentenceType = sentenceType,
  listDisplayName = listDisplayName,
)

fun Offence.toApi() = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Offence(
  id = this.id,
  code = this.code,
  description = this.description,
  offenceType = this.offenceType,
  revisionId = this.revisionId,
  startDate = this.startDate,
  endDate = this.endDate,
  homeOfficeStatsCode = this.homeOfficeStatsCode,
  homeOfficeDescription = this.homeOfficeDescription,
  changedDate = this.changedDate.toString(),
  loadDate = this.loadDate.toString(),
  schedules = this.schedules?.map {
    uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LinkedScheduleDetails(
      id = it.id,
      act = it.act,
      code = it.code,
      url = it.url,
      partNumber = it.partNumber,
      paragraphNumber = it.paragraphNumber,
      paragraphTitle = it.paragraphTitle,
      lineReference = it.lineReference,
      legislationText = it.legislationText,
    )
  },
  isChild = this.isChild,
  parentOffenceId = this.parentOffenceId,
  childOffenceIds = this.childOffenceIds,
  legislation = this.legislation,
  maxPeriodIsLife = this.maxPeriodIsLife,
  maxPeriodOfIndictmentYears = this.maxPeriodOfIndictmentYears,
  custodialIndicator = this.custodialIndicator?.name,
)
