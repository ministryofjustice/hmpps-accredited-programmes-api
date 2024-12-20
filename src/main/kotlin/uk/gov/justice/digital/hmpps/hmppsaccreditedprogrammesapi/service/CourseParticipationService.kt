package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseParticipationService
@Autowired
constructor(
  private val courseParticipationRepository: CourseParticipationRepository,
) {
  fun createCourseParticipation(courseParticipation: CourseParticipationEntity): CourseParticipationEntity? =
    courseParticipation.let {
      courseParticipationRepository.save(it)
    }

  fun getCourseParticipationById(historicCourseParticipationId: UUID): CourseParticipationEntity? =
    courseParticipationRepository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipationById(
    historicCourseParticipationId: UUID,
    update: CourseParticipationUpdate,
  ): CourseParticipationEntity =
    courseParticipationRepository
      .getReferenceById(historicCourseParticipationId)
      .applyUpdate(update)

  fun getCourseParticipationsByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity> =
    courseParticipationRepository.findByPrisonNumber(prisonNumber)

  fun deleteCourseParticipationById(historicCourseParticipationId: UUID) {
    courseParticipationRepository.deleteById(historicCourseParticipationId)
  }

  fun getCourseParticipationsByPrisonNumberAndStatus(prisonNumber: String, outcomeStatus: List<CourseStatus>): List<CourseParticipationEntity> {
    return courseParticipationRepository.findByPrisonNumberAndOutcomeStatusIn(prisonNumber, outcomeStatus)
  }

  fun getCourseParticipationHistoryByReferralId(uuid: UUID): List<CourseParticipationEntity> {
    return courseParticipationRepository.findByReferralId(uuid)
  }

  fun updateDraftHistoryForSubmittedReferral(referralId: UUID) {
    // Once a referral has been submitted, course participation records associated with that referral should no
    // longer be marked as draft
    courseParticipationRepository.updateDraftStatusByReferralId(referralId)
  }

  fun deleteAllCourseParticipationsForReferral(referralId: UUID) {
    courseParticipationRepository.deleteByReferralId(referralId)
  }
}

private fun CourseParticipationEntity.applyUpdate(update: CourseParticipationUpdate): CourseParticipationEntity =
  apply {
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
