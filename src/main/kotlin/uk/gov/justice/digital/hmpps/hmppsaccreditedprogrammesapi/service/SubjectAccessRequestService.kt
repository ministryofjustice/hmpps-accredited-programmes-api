package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class SubjectAccessRequestService(
  private val repository: ReferralRepository,
  private val courseParticipationRepository: CourseParticipationRepository,
) {

  fun getPrisonContentFor(prisonerNumber: String, fromDate: LocalDate?, toDate: LocalDate?) = HmppsSubjectAccessRequestContent(
    Content(
      repository.getSarReferrals(prisonerNumber).filter { referral ->
        val afterFromDate = fromDate?.let { referral.submittedOn?.isAfter(it.atStartOfDay()) } ?: true
        val beforeToDate = toDate?.let { referral.submittedOn?.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
        afterFromDate && beforeToDate
      }.toSarReferral(),
      courseParticipationRepository.getSarParticipations(prisonerNumber).filter { referral ->
        val afterFromDate = fromDate?.let { referral.createdDateTime.isAfter(it.atStartOfDay()) } ?: true
        val beforeToDate = toDate?.let { referral.createdDateTime.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
        afterFromDate && beforeToDate
      }
        .toSarParticipation(),
    ),
  )
}

data class HmppsSubjectAccessRequestContent(
  val content: Content,
)

data class Content(
  val referrals: List<SarReferral>,
  val courseParticipation: List<SarCourseParticipation>,
)

data class SarReferral(
  val prisonerNumber: String,
  val oasysConfirmed: Boolean,
  val statusCode: String?,
  val hasReviewedProgrammeHistory: Boolean?,
  val additionalInformation: String?,
  val submittedOn: LocalDateTime?,
  val referrerOverrideReason: String?,
  val referrerUsername: String?,
  val courseName: String?,
  val audience: String?,
  val courseOrganisation: String?,
  val originalReferralId: UUID?,
)

data class SarCourseParticipation(
  val prisonNumber: String,
  val yearStarted: Int?,
  val source: String?,
  val type: String?,
  val outcomeStatus: String?,
  val yearCompleted: Int?,
  val location: String?,
  val detail: String?,
  val courseName: String?,
  val createdByUser: String?,
  val createdDateTime: LocalDateTime?,
  val updatedByUser: String?,
  val updatedDateTime: LocalDateTime?,
)

private fun List<CourseParticipationEntity>.toSarParticipation(): List<SarCourseParticipation> = map {
  SarCourseParticipation(
    prisonNumber = it.prisonNumber,
    source = it.source,
    type = it.setting?.type?.name,
    outcomeStatus = it.outcome?.status?.name,
    yearStarted = it.outcome?.yearStarted?.value,
    yearCompleted = it.outcome?.yearCompleted?.value,
    location = it.setting?.location,
    detail = it.detail,
    courseName = it.courseName,
    createdByUser = it.createdByUsername,
    createdDateTime = it.createdDateTime,
    updatedByUser = it.lastModifiedByUsername,
    updatedDateTime = it.lastModifiedDateTime,
  )
}

private fun List<ReferralEntity>.toSarReferral(): List<SarReferral> = map {
  SarReferral(
    it.prisonNumber,
    it.oasysConfirmed,
    it.status,
    it.hasReviewedProgrammeHistory,
    it.additionalInformation,
    it.submittedOn,
    it.referrerOverrideReason,
    it.referrer.username,
    it.offering.course.name,
    it.offering.course.audience,
    it.offering.organisationId,
    it.originalReferralId,
  )
}
