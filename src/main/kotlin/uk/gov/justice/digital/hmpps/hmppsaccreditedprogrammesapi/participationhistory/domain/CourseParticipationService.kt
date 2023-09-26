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

  fun getCourseParticipation(courseParticipationId: UUID): CourseParticipation? =
    repository.findById(courseParticipationId).getOrNull()

  fun updateCourseParticipation(courseParticipationId: UUID, update: CourseParticipationUpdate): CourseParticipation =
    repository
      .getReferenceById(courseParticipationId)
      .applyUpdate(update)

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipation> = repository.findByPrisonNumber(prisonNumber)
  fun deleteCourseParticipation(courseParticipationId: UUID) {
    repository.deleteById(courseParticipationId)
  }

  companion object {
    private fun CourseParticipation.applyUpdate(update: CourseParticipationUpdate): CourseParticipation =
      this.apply {
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
