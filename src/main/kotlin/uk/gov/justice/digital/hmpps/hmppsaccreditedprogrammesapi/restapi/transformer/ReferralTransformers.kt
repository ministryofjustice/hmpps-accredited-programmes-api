package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.StaffDetail
import java.time.ZoneOffset
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralUpdate as ApiReferralUpdate

fun ReferralEntity.toApi(status: ReferralStatusRefData, staffDetail: StaffDetail?): ApiReferral = ApiReferral(
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
  primaryPrisonOffenderManager = staffDetail,
  overrideReason = overrideReason,
  originalReferralId = originalReferralId,
  hasLdc = hasLdc,
  hasLdcBeenOverriddenByProgrammeTeam = hasLdcBeenOverriddenByProgrammeTeam,
)

fun ReferralEntity.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offering.id!!,
  prisonNumber = prisonNumber,
  referrerUsername = referrer.username,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  additionalInformation = additionalInformation,
  status = status,
  overrideReason = overrideReason,
  originalReferralId = originalReferralId,
  hasLdc = hasLdc,
  hasLdcBeenOverriddenByProgrammeTeam = hasLdcBeenOverriddenByProgrammeTeam,
)

fun StaffEntity.toApi() = StaffDetail(
  staffId = staffId,
  firstName = firstName,
  lastName = lastName,
  primaryEmail = primaryEmail,
  username = username,
  accountType = AccountType.valueOf(accountType),
)

fun ApiReferralUpdate.toDomain() = ReferralUpdate(
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  overrideReason = overrideReason,
  hasLdc = hasLdc,
  hasLdcBeenOverriddenByProgrammeTeam = hasLdcBeenOverriddenByProgrammeTeam ?: false,
)

fun ReferralUpdate.toApi() = ApiReferralUpdate(
  additionalInformation = additionalInformation,
  oasysConfirmed = oasysConfirmed,
  hasReviewedProgrammeHistory = hasReviewedProgrammeHistory,
  overrideReason = overrideReason,
  hasLdc = hasLdc,
  hasLdcBeenOverriddenByProgrammeTeam = hasLdcBeenOverriddenByProgrammeTeam ?: false,
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
  location = location,
  hasLdc = hasLdc,
)
