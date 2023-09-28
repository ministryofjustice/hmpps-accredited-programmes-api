package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import java.util.UUID

class CourseParticipationEntityFactory : Factory<CourseParticipation> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var courseId: Yielded<UUID?> = { UUID.randomUUID() }
  private var otherCourseName: Yielded<String?> = { null }
  private var source: Yielded<String?> = { null }
  private var setting: Yielded<CourseParticipationSetting> = { CourseParticipationSetting(type = CourseSetting.CUSTODY) }
  private var outcome: Yielded<CourseOutcome> = { CourseOutcome() }

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

  fun withSource(source: String?) = apply {
    this.source = { source }
  }

  fun withSetting(setting: CourseParticipationSetting) = apply {
    this.setting = { setting }
  }

  fun withOutcome(outcome: CourseOutcome) = apply {
    this.outcome = { outcome }
  }

  override fun produce(): CourseParticipation {
    return CourseParticipation(
      id = this.id(),
      prisonNumber = this.prisonNumber(),
      courseId = this.courseId(),
      otherCourseName = this.otherCourseName(),
      source = this.source(),
      setting = this.setting(),
      outcome = this.outcome(),
    )
  }
}
