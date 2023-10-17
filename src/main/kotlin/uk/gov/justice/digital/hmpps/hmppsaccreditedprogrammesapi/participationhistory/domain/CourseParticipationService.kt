package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories.JpaCourseParticipationRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseParticipationService
@Autowired
constructor (
  private val courseParticipationRepository: JpaCourseParticipationRepository,
) {
  fun createCourseParticipation(courseParticipation: CourseParticipationEntity): CourseParticipationEntity? =
    courseParticipation.let {
      it.assertOnlyCourseIdOrCourseNamePresent()
      courseParticipationRepository.save(it)
    }

  fun getCourseParticipationById(historicCourseParticipationId: UUID): CourseParticipationEntity? =
    courseParticipationRepository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipationById(historicCourseParticipationId: UUID, update: CourseParticipationUpdate): CourseParticipationEntity =
    courseParticipationRepository
      .getReferenceById(historicCourseParticipationId)
      .applyUpdate(update)

  fun getCourseParticipationsByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity> = courseParticipationRepository.findByPrisonNumber(prisonNumber)
  fun deleteCourseParticipationById(historicCourseParticipationId: UUID) {
    courseParticipationRepository.deleteById(historicCourseParticipationId)
  }
}

private fun CourseParticipationEntity.applyUpdate(update: CourseParticipationUpdate): CourseParticipationEntity =
  apply {
    courseName = update.courseName
    courseId = update.courseId
    otherCourseName = update.otherCourseName
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
