package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
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
  private val courseRepository: CourseRepository,
  private val staffRepository: StaffRepository,
  private val organisationRepository: OrganisationRepository,
  private val staffLookupService: StaffLookupService,

) : HmppsPrisonSubjectAccessRequestService {

  private val log = LoggerFactory.getLogger(SubjectAccessRequestService::class.java)

  override fun getPrisonContentFor(prn: String, fromDate: LocalDate?, toDate: LocalDate?): HmppsSubjectAccessRequestContent? {
    val filteredReferrals = referralRepository.getSarReferrals(prn).filter { referral ->
      val afterFromDate = fromDate?.let { referral.submittedOn?.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { referral.submittedOn?.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }

    val filteredParticipations = courseParticipationRepository.getSarParticipations(prn).filter { courseParticipation ->
      val afterFromDate = fromDate?.let { courseParticipation.createdDateTime.isAfter(it.atStartOfDay()) } ?: true
      val beforeToDate = toDate?.let { courseParticipation.createdDateTime.isBefore(it.plusDays(1).atStartOfDay()) } ?: true
      afterFromDate && beforeToDate
    }

    // Batch-load every referral referenced by an `originalReferralId` on the
    // filtered set in a single `WHERE referral_id IN (?)` query. Missing IDs
    // (referential-integrity drift – e.g. an original hard-deleted outside
    // of the normal soft-delete flow) are silently dropped by the IN clause
    // and logged as a WARN so they remain visible in production telemetry.
    val originalReferralIds = filteredReferrals.mapNotNull { it.originalReferralId }.toSet()
    val originalsById: Map<UUID, ReferralEntity> = if (originalReferralIds.isEmpty()) {
      emptyMap()
    } else {
      referralRepository.findAllById(originalReferralIds).associateBy { it.id!! }
    }
    val unresolvedOriginals = originalReferralIds - originalsById.keys
    if (unresolvedOriginals.isNotEmpty()) {
      log.warn(
        "SAR: {} originalReferralId(s) referenced by filtered referrals could not be loaded: {}",
        unresolvedOriginals.size,
        unresolvedOriginals,
      )
    }

    // Resolve every staff surname referenced by the SAR payload in exactly two
    // queries (one by-username, one by-staff-id), rather than the previous
    // per-row point-lookup pattern which produced O(N) queries per referral /
    // course participation row. The original-referral set is folded in here
    // so per-original referrer surnames come for free from the same two
    // queries.
    val staffSurnames = resolveStaffSurnames(
      filteredReferrals + originalsById.values,
      filteredParticipations,
    )

    // Resolve organisations across the current *and* original referrals in a
    // single `WHERE code IN (?)` query. `organisationsByCode` is reused for
    // both the top-level `Content.organisations` list (order preserved from
    // the filtered referrals only, matching the previous per-code behaviour)
    // and every `SarOriginalReferral.organisationName` lookup below.
    val codesFromFiltered = filteredReferrals.mapNotNull { it.offering?.organisationId }.distinct()
    val allOrgCodes = buildSet {
      addAll(codesFromFiltered)
      originalsById.values.forEach { it.offering?.organisationId?.let(::add) }
    }
    val organisationsByCode: Map<String, OrganisationEntity> = if (allOrgCodes.isEmpty()) {
      emptyMap()
    } else {
      organisationRepository.findAllByCodeIn(allOrgCodes).associateBy { it.code }
    }
    val organisationNamesByCode: Map<String, String> = organisationsByCode.mapValues { it.value.name }

    return HmppsSubjectAccessRequestContent(
      content = Content(
        referrals = filteredReferrals.toSarReferral(staffSurnames, originalsById, organisationNamesByCode),
        courseParticipation = filteredParticipations.toSarParticipation(staffSurnames),
        courses = courseRepository.getSarCourses(prn).toSarCourse(),
        staff = staffRepository.findByPrisonNumber(prn).distinctBy { it.username }.map { it.toSarStaff() },
        organisations = codesFromFiltered.mapNotNull { organisationsByCode[it]?.toSarOrganisation() },
      ),

    )
  }

  /**
   * Pre-resolves every staff surname referenced across the SAR entity
   * collections that carry staff identifiers, collapsing what used to be
   * O(rows) point-lookups into two batch queries.
   */
  private fun resolveStaffSurnames(
    referrals: List<ReferralEntity>,
    participations: List<CourseParticipationEntity>,
  ): StaffSurnames {
    val usernames = buildSet {
      referrals.forEach { add(it.referrer.username) }
      participations.forEach {
        add(it.createdByUsername)
        it.lastModifiedByUsername?.let(::add)
      }
    }
    val staffIds = buildSet {
      referrals.forEach {
        it.primaryPomStaffId?.let(::add)
        it.secondaryPomStaffId?.let(::add)
      }
    }
    return StaffSurnames(
      byUsername = staffLookupService.resolveSurnamesByUsername(usernames),
      byStaffId = staffLookupService.resolveSurnamesByStaffId(staffIds),
    )
  }

  /**
   * In-memory view of the two staff-surname maps built once per SAR request.
   * Both lookup helpers short-circuit on null so mappers can consult them
   * uniformly regardless of whether the source column is nullable.
   */
  private data class StaffSurnames(
    val byUsername: Map<String, String>,
    val byStaffId: Map<BigInteger, String>,
  ) {
    fun forUsername(username: String?): String? = username?.let { byUsername[it] }
    fun forStaffId(staffId: BigInteger?): String? = staffId?.let { byStaffId[it] }
  }

  data class Content(
    val referrals: List<SarReferral>,
    val courseParticipation: List<SarCourseParticipation>,
    val courses: List<SarCourse>,
    val staff: List<SarStaff>,
    val organisations: List<SarOrganisation>,
  )

  data class SarReferral(
    val oasysConfirmed: Boolean,
    val statusCode: String?,
    val hasReviewedProgrammeHistory: Boolean?,
    val additionalInformation: String?,
    val submittedOn: LocalDateTime?,
    val primaryPomStaffSurname: String?,
    val secondaryPomStaffSurname: String?,
    val referrerOverrideReason: String?,
    val referrerUsername: String?,
    val hasLdc: Boolean?,
    val hasLdcBeenOverriddenByProgrammeTeam: Boolean,
    val hasReviewedAdditionalInformation: Boolean?,
    val originalReferral: SarOriginalReferral?,
  )

  /**
   * Enriched view of the referral this record supersedes (typically a WITHDRAWN
   * referral that was re-submitted onto a different pathway). Rendered as a
   * nested block on each [SarReferral] so a subject can understand what a bare
   * `originalReferralId` UUID refers to; `null` when the parent referral has
   * no `originalReferralId` or when the referenced row could not be loaded.
   *
   * Every field mirrors the corresponding field on the parent [SarReferral]
   * (or the resolved organisation / staff surname) so the mustache template
   * can render the block with the same `optionalValue` conventions.
   */
  data class SarOriginalReferral(
    val courseName: String?,
    val organisationName: String?,
    val submittedOn: LocalDateTime?,
    val statusCode: String?,
    val referrerSurname: String?,
    val referrerOverrideReason: String?,
    val hasLdc: Boolean?,
    val additionalInformation: String?,
  )

  data class SarCourseParticipation(
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

  data class SarCourse(
    val name: String,
  )

  data class SarStaff(
    val lastName: String,
  )

  private fun List<CourseParticipationEntity>.toSarParticipation(surnames: StaffSurnames): List<SarCourseParticipation> = map {
    SarCourseParticipation(
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
      createdByUser = surnames.forUsername(it.createdByUsername),
      createdDateTime = it.createdDateTime,
      updatedByUser = surnames.forUsername(it.lastModifiedByUsername),
      updatedDateTime = it.lastModifiedDateTime,
    )
  }

  private fun List<ReferralEntity>.toSarReferral(
    surnames: StaffSurnames,
    originalsById: Map<UUID, ReferralEntity>,
    organisationNamesByCode: Map<String, String>,
  ): List<SarReferral> = map {
    SarReferral(
      it.oasysConfirmed,
      it.status,
      it.hasReviewedProgrammeHistory,
      it.additionalInformation,
      it.submittedOn,
      surnames.forStaffId(it.primaryPomStaffId),
      surnames.forStaffId(it.secondaryPomStaffId),
      it.referrerOverrideReason,
      surnames.forUsername(it.referrer.username),
      it.hasLdc,
      it.hasLdcBeenOverriddenByProgrammeTeam,
      it.hasReviewedAdditionalInformation,
      originalReferral = it.originalReferralId?.let { originalId ->
        originalsById[originalId]?.toSarOriginalReferral(surnames, organisationNamesByCode)
      },
    )
  }

  private fun ReferralEntity.toSarOriginalReferral(
    surnames: StaffSurnames,
    organisationNamesByCode: Map<String, String>,
  ): SarOriginalReferral = SarOriginalReferral(
    courseName = offering?.course?.name,
    organisationName = offering?.organisationId?.let { organisationNamesByCode[it] },
    submittedOn = submittedOn,
    statusCode = status,
    referrerSurname = surnames.forUsername(referrer.username),
    referrerOverrideReason = referrerOverrideReason,
    hasLdc = hasLdc,
    additionalInformation = additionalInformation,
  )

  private fun List<CourseEntity>.toSarCourse(): List<SarCourse> = map {
    SarCourse(
      name = it.name,
    )
  }

  private fun StaffEntity.toSarStaff() = SarStaff(
    lastName = lastName,
  )

  data class SarOrganisation(
    val code: String,
    val name: String,
    val gender: String,
  )

  private fun OrganisationEntity.toSarOrganisation() = SarOrganisation(
    code = code,
    name = name,
    gender = gender.name,
  )
}
