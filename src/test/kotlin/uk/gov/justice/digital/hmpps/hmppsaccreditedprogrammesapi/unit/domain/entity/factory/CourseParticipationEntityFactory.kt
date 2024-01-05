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
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID

class CourseParticipationEntityFactory : Factory<CourseParticipationEntity> {
  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var courseName: Yielded<String?> = { null }
  private var source: Yielded<String?> = { null }
  private var detail: Yielded<String?> = { null }
  private var setting: Yielded<CourseParticipationSetting?> = { CourseParticipationSettingFactory().produce() }
  private var outcome: Yielded<CourseParticipationOutcome?> = { CourseParticipationOutcomeFactory().produce() }
  private var createdByUsername: Yielded<String> = { CLIENT_USERNAME }
  private var createdDateTime: Yielded<LocalDateTime> = { LocalDateTime.MIN }
  private var lastModifiedByUsername: Yielded<String?> = { null }
  private var lastModifiedDateTime: Yielded<LocalDateTime?> = { null }

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

  fun withCreatedDateTime(createdDateTime: LocalDateTime) = apply {
    this.createdDateTime = { createdDateTime }
  }

  fun withLastModifiedByUsername(lastModifiedByUsername: String?) = apply {
    this.lastModifiedByUsername = { lastModifiedByUsername }
  }

  fun withLastModifiedDateTime(lastModifiedDateTime: LocalDateTime?) = apply {
    this.lastModifiedDateTime = { lastModifiedDateTime }
  }

  override fun produce() = CourseParticipationEntity(
    id = this.id(),
    courseName = this.courseName(),
    prisonNumber = this.prisonNumber(),
    source = this.source(),
    detail = this.detail(),
    setting = this.setting(),
    outcome = this.outcome(),
    createdByUsername = this.createdByUsername(),
    createdDateTime = this.createdDateTime(),
    lastModifiedByUsername = this.lastModifiedByUsername(),
    lastModifiedDateTime = this.lastModifiedDateTime(),
  )
}

class CourseParticipationSettingFactory : Factory<CourseParticipationSetting> {
  private var location: Yielded<String?> = { null }
  private var type: Yielded<CourseSetting> = { CourseSetting.CUSTODY }

  fun withLocation(location: String?) = apply {
    this.location = { location }
  }

  fun withType(type: CourseSetting) = apply {
    this.type = { type }
  }

  override fun produce() = CourseParticipationSetting(
    location = this.location(),
    type = this.type(),
  )
}

class CourseParticipationOutcomeFactory : Factory<CourseParticipationOutcome> {
  private var status: Yielded<CourseStatus> = { CourseStatus.INCOMPLETE }
  private var yearStarted: Yielded<Year?> = { null }
  private var yearCompleted: Yielded<Year?> = { null }

  fun withStatus(status: CourseStatus) = apply {
    this.status = { status }
  }

  fun withYearStarted(yearStarted: Year?) = apply {
    this.yearStarted = { yearStarted }
  }

  fun withYearCompleted(yearCompleted: Year?) = apply {
    this.yearCompleted = { yearCompleted }
  }

  override fun produce() = CourseParticipationOutcome(
    status = this.status(),
    yearStarted = this.yearStarted(),
    yearCompleted = this.yearCompleted(),
  )
}
