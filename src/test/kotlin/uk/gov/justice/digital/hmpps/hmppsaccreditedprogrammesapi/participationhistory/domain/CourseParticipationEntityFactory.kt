package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import java.util.UUID

class CourseParticipationEntityFactory : Factory<CourseParticipationEntity> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var courseName: Yielded<String?> = { null }
  private var courseId: Yielded<UUID?> = { UUID.randomUUID() }
  private var otherCourseName: Yielded<String?> = { null }
  private var source: Yielded<String?> = { null }
  private var detail: Yielded<String?> = { null }
  private var setting: Yielded<CourseParticipationSetting> = { CourseParticipationSetting(type = CourseSetting.CUSTODY) }
  private var outcome: Yielded<CourseParticipationOutcome> = { CourseParticipationOutcome(status = CourseStatus.INCOMPLETE) }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }

  fun withCourseName(courseName: String?) = apply {
    this.courseName = { courseName }
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

  fun withDetail(detail: String?) = apply {
    this.detail = { detail }
  }

  fun withSetting(setting: CourseParticipationSetting) = apply {
    this.setting = { setting }
  }

  fun withOutcome(outcome: CourseParticipationOutcome) = apply {
    this.outcome = { outcome }
  }

  override fun produce(): CourseParticipationEntity {
    return CourseParticipationEntity(
      courseName = this.courseName(),
      id = this.id(),
      prisonNumber = this.prisonNumber(),
      courseId = this.courseId(),
      otherCourseName = this.otherCourseName(),
      source = this.source(),
      detail = this.detail(),
      setting = this.setting(),
      outcome = this.outcome(),
    )
  }
}
