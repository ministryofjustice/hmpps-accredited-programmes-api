package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.jparepo.JpaCourseParticipationHistoryRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseParticipationHistoryService(
  @Autowired val repository: JpaCourseParticipationHistoryRepository,
) {
  fun addCourseParticipation(courseParticipation: CourseParticipationHistory): CourseParticipationHistory? =
    courseParticipation.let {
      it.assertOnlyCourseIdOrCourseNamePresent()
      repository.save(it)
    }

  fun getCourseParticipationHistory(historicCourseParticipationId: UUID): CourseParticipationHistory? =
    repository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipationHistory(historicCourseParticipationId: UUID, update: CourseParticipationHistoryUpdate): CourseParticipationHistory =
    repository
      .getReferenceById(historicCourseParticipationId)
      .applyUpdate(update)

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationHistory> = repository.findByPrisonNumber(prisonNumber)
  fun deleteCourseParticipation(historicCourseParticipationId: UUID) {
    repository.deleteById(historicCourseParticipationId)
  }
}

private fun CourseParticipationHistory.applyUpdate(update: CourseParticipationHistoryUpdate): CourseParticipationHistory =
  apply {
    courseId = update.courseId
    otherCourseName = update.otherCourseName
    setting.run {
      type = update.setting.type
      location = update.setting.location
    }
    outcome.run {
      status = update.outcome.status
      detail = update.outcome.detail
      yearStarted = update.outcome.yearStarted
      yearCompleted = update.outcome.yearCompleted
    }
  }
