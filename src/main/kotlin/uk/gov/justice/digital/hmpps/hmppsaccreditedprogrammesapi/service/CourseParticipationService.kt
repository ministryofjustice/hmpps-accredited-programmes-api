package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.projection.CourseParticipationProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseParticipationService
@Autowired
constructor(
  private val courseParticipationRepository: CourseParticipationRepository,
  private val organisationRepository: OrganisationRepository,
) {
  fun createCourseParticipation(courseParticipation: CourseParticipationEntity): CourseParticipationEntity? = courseParticipation.let {
    courseParticipationRepository.save(it)
  }

  fun createOrUpdateCourseParticipation(referral: ReferralEntity) {
    val courseName = referral.offering.course.name
    val organisationEntity = organisationRepository.findOrganisationEntityByCode(referral.offering.organisationId)
      ?: throw IllegalArgumentException("Organisation not found for code: ${referral.offering.organisationId}")

    // If an existing course participation record exists for a particular referral and course, we should update it.
    // This should be done by comparing referralId and courseId ideally but the existing data set is incomplete
    // in this regard, hence the filter below
    val filteredParticipations = courseParticipationRepository.findByPrisonNumber(referral.prisonNumber)
      .filter {
        it.courseName == courseName &&
          it.outcome?.yearCompleted == null &&
          it.outcome?.status != CourseStatus.INCOMPLETE
      }

    when (filteredParticipations.size) {
      1 -> updateExistingParticipation(filteredParticipations.first(), referral, organisationEntity)
      else -> {
        // Create a new course participation if none or multiples exist
        val newParticipation = buildCourseParticipation(referral, organisationEntity)
        courseParticipationRepository.save(newParticipation)
      }
    }
  }

  private fun updateExistingParticipation(
    existingParticipation: CourseParticipationEntity,
    referral: ReferralEntity,
    organisationEntity: OrganisationEntity,
  ) {
    existingParticipation.apply {
      referralId = referral.id
      courseId = referral.offering.course.id
      source = referral.referrer.username
      detail = referral.additionalInformation
      lastModifiedDateTime = LocalDateTime.now()
      setting = CourseParticipationSetting(
        location = organisationEntity.name,
        type = CourseSetting.CUSTODY,
      )
      outcome = buildCourseParticipationOutcomeByStatus(referral.status)
        .also {
          if (existingParticipation.outcome?.yearStarted != null) {
            it?.yearStarted = existingParticipation.outcome?.yearStarted
          }
        }
    }
    courseParticipationRepository.save(existingParticipation)
  }

  private fun buildCourseParticipation(referralEntity: ReferralEntity, organisationEntity: OrganisationEntity): CourseParticipationEntity = CourseParticipationEntity(
    referralId = referralEntity.id,
    prisonNumber = referralEntity.prisonNumber,
    courseId = referralEntity.offering.course.id,
    courseName = referralEntity.offering.course.name,
    source = referralEntity.referrer.username,
    detail = referralEntity.additionalInformation,
    createdDateTime = LocalDateTime.now(),
    setting = CourseParticipationSetting(
      location = organisationEntity.name,
      type = CourseSetting.CUSTODY,
    ),
    outcome = buildCourseParticipationOutcomeByStatus(referralEntity.status),
  )

  private fun buildCourseParticipationOutcomeByStatus(referralStatus: String): CourseParticipationOutcome? = when (referralStatus) {
    ReferralStatus.PROGRAMME_COMPLETE.name -> CourseParticipationOutcome(
      CourseStatus.COMPLETE,
      yearCompleted = Year.now(),
    )

    ReferralStatus.DESELECTED.name -> CourseParticipationOutcome(CourseStatus.INCOMPLETE)
    else -> null
  }

  fun getCourseParticipationById(historicCourseParticipationId: UUID): CourseParticipationEntity? = courseParticipationRepository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipationById(
    historicCourseParticipationId: UUID,
    update: CourseParticipationUpdate,
  ): CourseParticipationEntity = courseParticipationRepository
    .getReferenceById(historicCourseParticipationId)
    .applyUpdate(update)

  fun getCourseParticipationsByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity> = courseParticipationRepository.findByPrisonNumber(prisonNumber)
    .filterNot { it.isDraft == true }

  fun deleteCourseParticipationById(historicCourseParticipationId: UUID) {
    courseParticipationRepository.deleteById(historicCourseParticipationId)
  }

  fun getCourseParticipationsByPrisonNumberAndStatus(prisonNumber: String, outcomeStatus: List<CourseStatus>): List<CourseParticipationEntity> = courseParticipationRepository.findByPrisonNumberAndOutcomeStatusIn(prisonNumber, outcomeStatus)

  fun getCourseParticipationHistoryByReferralId(uuid: UUID): List<CourseParticipationProjection> = courseParticipationRepository.findCourseParticipationByReferralId(uuid)

  fun updateDraftHistoryForSubmittedReferral(referralId: UUID) {
    // Once a referral has been submitted, course participation records associated with that referral should no
    // longer be marked as draft
    courseParticipationRepository.updateDraftStatusByReferralId(referralId)
  }

  fun deleteAllCourseParticipationsForReferral(referralId: UUID) {
    courseParticipationRepository.deleteByReferralId(referralId)
  }

  fun deleteAllCourseParticipationsForReferralIds(referralIds: List<UUID>) {
    courseParticipationRepository.deleteCourseParticipationEntitiesByReferralIdIsIn(referralIds)
  }
}

private fun CourseParticipationEntity.applyUpdate(update: CourseParticipationUpdate): CourseParticipationEntity = apply {
  courseName = update.courseName
  source = update.source
  detail = update.detail

  update.setting?.let {
    if (setting == null) {
      setting = it
    } else {
      setting?.location = it.location
      setting?.type = it.type
    }
  }

  update.outcome?.let {
    if (outcome == null) {
      outcome = it
    } else {
      outcome?.status = it.status
      outcome?.yearStarted = it.yearStarted
      outcome?.yearCompleted = it.yearCompleted
    }
  }
}
