package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomStringUpperCaseWithNumbers
import java.time.Year
import java.util.UUID

class CourseParticipationHistoryEntityFactory : Factory<CourseParticipationHistory> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomStringUpperCaseWithNumbers(6) }
  private var courseId: Yielded<UUID?> = { UUID.randomUUID() }
  private var otherCourseName: Yielded<String?> = { null }
  private var yearStarted: Yielded<Year?> = { null }
  private var source: Yielded<String?> = { null }
  private var setting: Yielded<CourseSetting?> = { null }
  private var outcome: Yielded<CourseOutcome?> = { null }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }
  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }
  fun withCourseId(courseId: UUID?) = apply {
    this.courseId = { courseId }
  }
  fun withOtherCourseName(otherCourseName: String?) = apply {
    this.otherCourseName = { otherCourseName }
  }
  fun withYearStarted(yearStarted: Year?) = apply {
    this.yearStarted = { yearStarted }
  }
  fun withSource(source: String?) = apply {
    this.source = { source }
  }
  fun withSetting(setting: CourseSetting?) = apply {
    this.setting = { setting }
  }
  fun withOutcome(outcome: CourseOutcome?) = apply {
    this.outcome = { outcome }
  }

  override fun produce(): CourseParticipationHistory {
    return CourseParticipationHistory(
      id = this.id(),
      prisonNumber = this.prisonNumber(),
      courseId = this.courseId(),
      otherCourseName = this.otherCourseName(),
      yearStarted = this.yearStarted(),
      source = this.source(),
      setting = this.setting(),
      outcome = this.outcome(),
    )
  }
}
