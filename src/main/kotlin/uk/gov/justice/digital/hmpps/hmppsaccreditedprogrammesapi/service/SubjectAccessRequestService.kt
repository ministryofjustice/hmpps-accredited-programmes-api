package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PniResultRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class SubjectAccessRequestService(
  private val referralRepository: ReferralRepository,
  private val courseParticipationRepository: CourseParticipationRepository,
  private val auditRepository: AuditRepository,
  private val courseRepository: CourseRepository,
  private val pniResultRepository: PniResultRepository,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val staffRepository: StaffRepository,

) : HmppsPrisonSubjectAccessRequestService {

  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent? = HmppsSubjectAccessRequestContent(
    content = Content(
      referralRepository.getSarReferrals(prn).filter { referral ->
        val afterFromDate = fromDate?.let { referral.submittedOn?.isAfter(it.atStartOfDay()) } ?: true
        val beforeToDate = toDate?.let { referral.submittedOn?.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
        afterFromDate && beforeToDate
      }.toSarReferral(),
      courseParticipationRepository.getSarParticipations(prn).filter { courseParticipation ->
        val afterFromDate = fromDate?.let { courseParticipation.createdDateTime.isAfter(it.atStartOfDay()) } ?: true
        val beforeToDate = toDate?.let { courseParticipation.createdDateTime.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
        afterFromDate && beforeToDate
      }.toSarParticipation(),
      auditRepository.getSarAuditRecords(prn).toSarAudit(),
      courseRepository.getSarCourses(prn).toSarCourse(),
      pniResultRepository.findAllByPrisonNumber(prn).toSarPniResult(),
      referralStatusHistoryRepository.findByPrisonNumber(prn).toSarReferralStatusHistory(),
      staffRepository.findByPrisonNumber(prn).toSarStaff(),
    ),

  )

  data class Content(
    val referrals: List<SarReferral>,
    val courseParticipation: List<SarCourseParticipation>,
    val auditRecords: List<SarAuditRecord>,
    val courses: List<SarCourse>,
    val pniResults: List<SarPniResult>,
    val referralStatusHistory: List<SarReferralStatusHistoryEntity>,
    val staff: List<SarStaff>,
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
    val deleted: Boolean,
    val version: Long,
  )

  data class SarCourseParticipation(
    val prisonNumber: String,
    val referralId: UUID?,
    val isDraft: Boolean?,
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

  data class SarAuditRecord(
    val referrerUsername: String?,
    val auditUsername: String,
  )

  data class SarCourse(
    val description: String?,
    val alternateName: String?,
    val audience: String,
    val listDisplayName: String?,
    val intensity: String?,
  )

  data class SarPniResult(
    val pniResultJson: String?,
  )

  data class SarReferralStatusHistoryEntity(
    val version: Long,
  )

  data class SarStaff(
    val id: UUID?,
    val staffId: BigInteger?,
    val firstName: String,
    val lastName: String,
    val primaryEmail: String?,
    val username: String,
  )

  private fun List<CourseParticipationEntity>.toSarParticipation(): List<SarCourseParticipation> = map {
    SarCourseParticipation(
      prisonNumber = it.prisonNumber,
      referralId = it.referralId,
      isDraft = it.isDraft,
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
      it.deleted,
      it.version,
    )
  }

  private fun List<AuditEntity>.toSarAudit(): List<SarAuditRecord> = map {
    SarAuditRecord(
      referrerUsername = it.referrerUsername,
      auditUsername = it.auditUsername,
    )
  }

  private fun List<CourseEntity>.toSarCourse(): List<SarCourse> = map {
    SarCourse(
      description = it.description,
      alternateName = it.alternateName,
      audience = it.audience,
      listDisplayName = it.listDisplayName,
      intensity = it.intensity,
    )
  }

  private fun List<PniResultEntity>.toSarPniResult(): List<SarPniResult> = map {
    SarPniResult(
      pniResultJson = it.pniResultJson,
    )
  }

  private fun List<ReferralStatusHistoryEntity>.toSarReferralStatusHistory(): List<SarReferralStatusHistoryEntity> = map {
    SarReferralStatusHistoryEntity(
      version = it.version,
    )
  }

  private fun List<StaffEntity>.toSarStaff(): List<SarStaff> = map {
    SarStaff(
      id = it.id,
      staffId = it.staffId,
      username = it.username,
      firstName = it.firstName,
      lastName = it.lastName,
      primaryEmail = it.primaryEmail,
    )
  }
}
