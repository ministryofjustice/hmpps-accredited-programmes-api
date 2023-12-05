package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import java.util.UUID

class CourseParticipationEntityFactory : Factory<CourseParticipationEntity> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var courseName: Yielded<String?> = { null }
  private var source: Yielded<String?> = { null }
  private var detail: Yielded<String?> = { null }
  private var setting: Yielded<CourseParticipationSetting?> = { CourseParticipationSetting(type = CourseSetting.CUSTODY) }
  private var outcome: Yielded<CourseParticipationOutcome?> = { CourseParticipationOutcome(status = CourseStatus.INCOMPLETE) }
  private var createdByUsername: Yielded<String> = { CLIENT_USERNAME }
  private var lastModifiedByUsername: Yielded<String?> = { null }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }

  fun withCourseName(courseName: String?) = apply {
    this.courseName = { courseName }
  }

  fun withSource(source: String?) = apply {
    this.source = { source }
  }

  fun withDetail(detail: String?) = apply {
    this.detail = { detail }
  }

  fun withSetting(setting: CourseParticipationSetting?) = apply {
    this.setting = { setting }
  }

  fun withOutcome(outcome: CourseParticipationOutcome?) = apply {
    this.outcome = { outcome }
  }

  fun withCreatedByUsername(createdByUsername: String) = apply {
    this.createdByUsername = { createdByUsername }
  }

  fun withLastModifiedByUsername(lastModifiedByUsername: String) = apply {
    this.lastModifiedByUsername = { lastModifiedByUsername }
  }

  override fun produce(): CourseParticipationEntity {
    return CourseParticipationEntity(
      id = this.id(),
      courseName = this.courseName(),
      prisonNumber = this.prisonNumber(),
      source = this.source(),
      detail = this.detail(),
      setting = this.setting(),
      outcome = this.outcome(),
      createdByUsername = this.createdByUsername(),
      lastModifiedByUsername = this.lastModifiedByUsername(),
    )
  }
}
