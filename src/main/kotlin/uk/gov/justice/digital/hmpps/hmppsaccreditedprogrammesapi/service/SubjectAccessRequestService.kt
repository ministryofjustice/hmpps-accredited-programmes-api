package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OasysPniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OasysPniResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
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
  private val personRepository: PersonRepository,
  private val oasysPniResultEntityRepository: OasysPniResultEntityRepository,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val staffRepository: StaffRepository,
  private val organisationRepository: OrganisationRepository,

) : HmppsPrisonSubjectAccessRequestService {

  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent? {
    val filteredReferrals = referralRepository.getSarReferrals(prn).filter { referral ->
      val afterFromDate = fromDate?.let { referral.submittedOn?.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { referral.submittedOn?.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }

    val referralStatusHistory = referralStatusHistoryRepository.findByPrisonNumber(prn)
    val selectedSexualOffenceDetails = filteredReferrals
      .flatMap { it.selectedSexualOffenceDetails }
      .distinctBy { it.id }

    return HmppsSubjectAccessRequestContent(
      content = Content(
        referrals = filteredReferrals.toSarReferral(),
        courseParticipation = courseParticipationRepository.getSarParticipations(prn).filter { courseParticipation ->
          val afterFromDate = fromDate?.let { courseParticipation.createdDateTime.isAfter(it.atStartOfDay()) } ?: true
          val beforeToDate = toDate?.let { courseParticipation.createdDateTime.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
          afterFromDate && beforeToDate
        }.toSarParticipation(),
        auditRecords = auditRepository.getSarAuditRecords(prn).toSarAudit(),
        courses = courseRepository.getSarCourses(prn).toSarCourse(),
        pniResults = pniResultRepository.findAllByPrisonNumber(prn).toSarPniResult(),
        person = personRepository.findPersonEntityByPrisonNumber(prn)?.toSarPerson(),
        oasysPniResults = oasysPniResultEntityRepository.findAllByPrisonNumber(prn).toSarOasysPniResult(),
        referralStatusHistory = referralStatusHistory.toSarReferralStatusHistory(),
        referralStatusReasons = referralStatusHistory.mapNotNull { it.reason }.distinctBy { it.code }.toSarReferralStatusReason(),
        selectedSexualOffenceDetails = selectedSexualOffenceDetails.toSarSelectedSexualOffenceDetails(),
        sexualOffenceDetails = selectedSexualOffenceDetails.mapNotNull { it.sexualOffenceDetails }.distinctBy { it.id }.toSarSexualOffenceDetails(),
        staff = staffRepository.findByPrisonNumber(prn).map { it.toSarStaff() }.distinctBy { it.staffId },
        organisations = filteredReferrals.mapNotNull { it.offering?.organisationId }
          .distinct()
          .mapNotNull { organisationRepository.findOrganisationEntityByCode(it)?.toSarOrganisation() },
      ),

    )
  }

  data class Content(
    val referrals: List<SarReferral>,
    val courseParticipation: List<SarCourseParticipation>,
    val auditRecords: List<SarAuditRecord>,
    val courses: List<SarCourse>,
    val pniResults: List<SarPniResult>,
    val person: SarPerson?,
    val oasysPniResults: List<SarOasysPniResult>,
    val referralStatusHistory: List<SarReferralStatusHistoryEntity>,
    val referralStatusReasons: List<SarReferralStatusReason>,
    val selectedSexualOffenceDetails: List<SarSelectedSexualOffenceDetails>,
    val sexualOffenceDetails: List<SarSexualOffenceDetails>,
    val staff: List<SarStaff>,
    val organisations: List<SarOrganisation>,
  )

  data class SarReferral(
    val prisonerNumber: String,
    val oasysConfirmed: Boolean,
    val statusCode: String?,
    val hasReviewedProgrammeHistory: Boolean?,
    val additionalInformation: String?,
    val submittedOn: LocalDateTime?,
    val primaryPomStaffId: BigInteger?,
    val secondaryPomStaffId: BigInteger?,
    val referrerOverrideReason: String?,
    val referrerUsername: String?,
    val originalReferralId: UUID?,
    val hasLdc: Boolean?,
    val hasLdcBeenOverriddenByProgrammeTeam: Boolean,
    val hasReviewedAdditionalInformation: Boolean?,
    val deleted: Boolean,
  )

  data class SarCourseParticipation(
    val prisonNumber: String,
    val isDraft: Boolean?, // should be here
    val otherCourseName: String?,
    val yearStarted: Int?,
    val source: String?,
    val type: String?,
    val outcomeStatus: String?,
    val outcomeDetail: String?,
    val yearCompleted: Int?,
    val location: String?,
    val detail: String?,
    val courseName: String?,
    val createdByUser: String?, // should be here
    val createdDateTime: LocalDateTime?,
    val updatedByUser: String?, // should be here
    val updatedDateTime: LocalDateTime?,
  )

  data class SarAuditRecord(
    val prisonNumber: String,
    val referrerUsername: String?,
    val referralStatusFrom: String?,
    val referralStatusTo: String?,
    val courseName: String?,
    val courseLocation: String?,
    val auditAction: String,
    val auditUsername: String,
    val auditDateTime: LocalDateTime,
  )

  data class SarCourse(
    val name: String,
  )

  data class SarPniResult(
    val prisonNumber: String,
    val crn: String?,
    val oasysAssessmentCompletedDate: LocalDateTime?,
    val programmePathway: String?,
    val needsClassification: String?,
    val overallNeedsScore: Int?,
    val riskClassification: String?,
    val pniAssessmentDate: LocalDateTime?,
    val pniValid: Boolean,
    val pniResultJson: String?, // should be here
    val basicSkillsScore: Int?,
  )

  data class SarPerson(
    val id: UUID?,
    val prisonNumber: String,
    val forename: String,
    val surname: String,
    val conditionalReleaseDate: LocalDate?,
    val paroleEligibilityDate: LocalDate?,
    val tariffExpiryDate: LocalDate?,
    val earliestReleaseDate: LocalDate?,
    val earliestReleaseDateType: String?,
    val indeterminateSentence: Boolean?,
    val nonDtoReleaseDateType: String?,
    val sentenceType: String?,
    val location: String?,
    val gender: String?,
  )

  data class SarOasysPniResult(
    val pniResultId: UUID,
    val prisonNumber: String,
    val oasysAssessmentId: Long?,
    val programmePathway: String?,
  )

  data class SarReferralStatusHistoryEntity(
    val id: UUID?,
    val referralId: UUID,
    val status: String,
    val previousStatus: String?,
    val category: String?,
    val reason: String?,
    val notes: String?,
    val statusStartDate: LocalDateTime,
    val statusEndDate: LocalDateTime?,
    val durationAtThisStatus: Long?,
    val username: String,
  )

  data class SarReferralStatusReason(
    val code: String,
    val referralStatusCategoryCode: String,
    val description: String,
    val active: Boolean,
    val deselectOpen: Boolean,
  )

  data class SarSelectedSexualOffenceDetails(
    val id: UUID?,
    val referralId: UUID?,
    val sexualOffenceDetailsId: UUID?,
  )

  data class SarSexualOffenceDetails(
    val id: UUID?,
    val category: String,
    val description: String,
    val score: Int,
  )

  data class SarStaff(
    val staffId: BigInteger?,
    val firstName: String,
    val lastName: String,
    val primaryEmail: String?,
    val username: String,
    val accountType: String,
  )

  private fun List<CourseParticipationEntity>.toSarParticipation(): List<SarCourseParticipation> = map {
    SarCourseParticipation(
      prisonNumber = it.prisonNumber,
      isDraft = it.isDraft,
      otherCourseName = it.otherCourseName,
      source = it.source,
      type = it.setting?.type?.name,
      outcomeStatus = it.outcome?.status?.name,
      outcomeDetail = it.outcomeDetail,
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
      it.primaryPomStaffId,
      it.secondaryPomStaffId,
      it.referrerOverrideReason,
      it.referrer.username,
      it.originalReferralId,
      it.hasLdc,
      it.hasLdcBeenOverriddenByProgrammeTeam,
      it.hasReviewedAdditionalInformation,
      it.deleted,
    )
  }

  private fun List<AuditEntity>.toSarAudit(): List<SarAuditRecord> = map {
    SarAuditRecord(
      prisonNumber = it.prisonNumber,
      referrerUsername = it.referrerUsername,
      referralStatusFrom = it.referralStatusFrom,
      referralStatusTo = it.referralStatusTo,
      courseName = it.courseName,
      courseLocation = it.courseLocation,
      auditAction = it.auditAction,
      auditUsername = it.auditUsername,
      auditDateTime = it.auditDateTime,
    )
  }

  private fun List<CourseEntity>.toSarCourse(): List<SarCourse> = map {
    SarCourse(
      name = it.name,
    )
  }

  private fun List<PniResultEntity>.toSarPniResult(): List<SarPniResult> = map {
    SarPniResult(
      prisonNumber = it.prisonNumber,
      crn = it.crn,
      oasysAssessmentCompletedDate = it.oasysAssessmentCompletedDate,
      programmePathway = it.programmePathway,
      needsClassification = it.needsClassification,
      overallNeedsScore = it.overallNeedsScore,
      riskClassification = it.riskClassification,
      pniAssessmentDate = it.pniAssessmentDate,
      pniValid = it.pniValid,
      pniResultJson = it.pniResultJson,
      basicSkillsScore = it.basicSkillsScore,
    )
  }

  private fun PersonEntity.toSarPerson(): SarPerson = SarPerson(
    id = id,
    prisonNumber = prisonNumber,
    forename = forename,
    surname = surname,
    conditionalReleaseDate = conditionalReleaseDate,
    paroleEligibilityDate = paroleEligibilityDate,
    tariffExpiryDate = tariffExpiryDate,
    earliestReleaseDate = earliestReleaseDate,
    earliestReleaseDateType = earliestReleaseDateType,
    indeterminateSentence = indeterminateSentence,
    nonDtoReleaseDateType = nonDtoReleaseDateType,
    sentenceType = sentenceType,
    location = location,
    gender = gender,
  )

  private fun List<OasysPniResultEntity>.toSarOasysPniResult(): List<SarOasysPniResult> = map {
    SarOasysPniResult(
      pniResultId = it.pniResultId,
      prisonNumber = it.prisonNumber,
      oasysAssessmentId = it.oasysAssessmentId,
      programmePathway = it.programmePathway,
    )
  }

  private fun List<ReferralStatusHistoryEntity>.toSarReferralStatusHistory(): List<SarReferralStatusHistoryEntity> = map {
    SarReferralStatusHistoryEntity(
      id = it.id,
      referralId = it.referralId,
      status = it.status.code,
      previousStatus = it.previousStatus?.code,
      category = it.category?.code,
      reason = it.reason?.code,
      notes = it.notes,
      statusStartDate = it.statusStartDate,
      statusEndDate = it.statusEndDate,
      durationAtThisStatus = it.durationAtThisStatus,
      username = it.username,
    )
  }

  private fun List<ReferralStatusReasonEntity>.toSarReferralStatusReason(): List<SarReferralStatusReason> = map {
    SarReferralStatusReason(
      code = it.code,
      referralStatusCategoryCode = it.referralStatusCategoryCode,
      description = it.description,
      active = it.active,
      deselectOpen = it.deselectOpen,
    )
  }

  private fun List<SelectedSexualOffenceDetailsEntity>.toSarSelectedSexualOffenceDetails(): List<SarSelectedSexualOffenceDetails> = map {
    SarSelectedSexualOffenceDetails(
      id = it.id,
      referralId = it.referral.id,
      sexualOffenceDetailsId = it.sexualOffenceDetails?.id,
    )
  }

  private fun List<SexualOffenceDetailsEntity>.toSarSexualOffenceDetails(): List<SarSexualOffenceDetails> = map {
    SarSexualOffenceDetails(
      id = it.id,
      category = it.category.name,
      description = it.description,
      score = it.score,
    )
  }

  private fun StaffEntity.toSarStaff() = SarStaff(
    staffId = staffId,
    firstName = firstName,
    lastName = lastName,
    primaryEmail = primaryEmail,
    username = username,
    accountType = accountType,
  )

  data class SarOrganisation(
    val id: String,
    val code: String,
    val name: String,
    val gender: String,
  )

  private fun OrganisationEntity.toSarOrganisation() = SarOrganisation(
    id = id.toString(),
    code = code,
    name = name,
    gender = gender.name,
  )
}
