package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories.JpaCourseParticipationRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseParticipationService(
  @Autowired val repository: JpaCourseParticipationRepository,
) {
  fun addCourseParticipation(courseParticipation: CourseParticipation): CourseParticipation? =
    courseParticipation.let {
      it.assertOnlyCourseIdOrCourseNamePresent()
      repository.save(it)
    }

  fun getCourseParticipation(historicCourseParticipationId: UUID): CourseParticipation? =
    repository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipation(historicCourseParticipationId: UUID, update: CourseParticipationUpdate): CourseParticipation =
    repository
      .getReferenceById(historicCourseParticipationId)
      .applyUpdate(update)

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipation> = repository.findByPrisonNumber(prisonNumber)
  fun deleteCourseParticipation(historicCourseParticipationId: UUID) {
    repository.deleteById(historicCourseParticipationId)
  }
}

private fun CourseParticipation.applyUpdate(update: CourseParticipationUpdate): CourseParticipation =
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
