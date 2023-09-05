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
  fun addCourseParticipation(courseParticipation: CourseParticipationHistory): UUID? =
    courseParticipation.let {
      it.assertOnlyCourseIdOrCourseNamePresent()
      repository.save(it).id
    }

  fun getCourseParticipationHistory(historicCourseParticipationId: UUID): CourseParticipationHistory? =
    repository.findById(historicCourseParticipationId).getOrNull()

  fun updateCourseParticipationHistory(historicCourseParticipationId: UUID, update: CourseParticipationHistoryUpdate) {
    repository
      .getReferenceById(historicCourseParticipationId)
      .applyUpdate(update)
  }

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationHistory> = repository.findByPrisonNumber(prisonNumber)

  companion object {
    private fun CourseParticipationHistory.applyUpdate(update: CourseParticipationHistoryUpdate) {
      yearStarted = update.yearStarted
      courseId = update.courseId
      otherCourseName = update.otherCourseName
      setting = update.setting
      if (outcome == null) {
        outcome = update.outcome?.let { CourseOutcome(status = it.status, detail = it.detail) }
      } else {
        outcome!!.status = update.outcome?.status
        outcome!!.detail = update.outcome?.detail
      }
    }
  }
}
