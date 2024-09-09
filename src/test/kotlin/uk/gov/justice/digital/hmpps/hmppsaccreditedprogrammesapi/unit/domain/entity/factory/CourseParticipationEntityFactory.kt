package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID
class CourseParticipationEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var prisonNumber: String = randomPrisonNumber()
  private var courseName: String? = null
  private var source: String? = null
  private var detail: String? = null
  private var setting: CourseParticipationSetting? = CourseParticipationSettingFactory().produce()
  private var outcome: CourseParticipationOutcome? = CourseParticipationOutcomeFactory().produce()
  private var createdByUsername: String = REFERRER_USERNAME
  private var createdDateTime: LocalDateTime = LocalDateTime.MIN
  private var lastModifiedByUsername: String? = null
  private var lastModifiedDateTime: LocalDateTime? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withCourseName(courseName: String?) = apply { this.courseName = courseName }
  fun withSource(source: String?) = apply { this.source = source }
  fun withDetail(detail: String?) = apply { this.detail = detail }
  fun withSetting(setting: CourseParticipationSetting?) = apply { this.setting = setting }
  fun withOutcome(outcome: CourseParticipationOutcome?) = apply { this.outcome = outcome }
  fun withCreatedByUsername(createdByUsername: String) = apply { this.createdByUsername = createdByUsername }
  fun withCreatedDateTime(createdDateTime: LocalDateTime) = apply { this.createdDateTime = createdDateTime }

  fun produce() = CourseParticipationEntity(
    id = this.id,
    courseName = this.courseName,
    prisonNumber = this.prisonNumber,
    source = this.source,
    detail = this.detail,
    setting = this.setting,
    outcome = this.outcome,
    createdByUsername = this.createdByUsername,
    createdDateTime = this.createdDateTime,
    lastModifiedByUsername = this.lastModifiedByUsername,
    lastModifiedDateTime = this.lastModifiedDateTime,
  )
}

class CourseParticipationSettingFactory {
  private var location: String? = null
  private var type: CourseSetting = CourseSetting.CUSTODY

  fun withLocation(location: String?) = apply { this.location = location }
  fun withType(type: CourseSetting) = apply { this.type = type }

  fun produce() = CourseParticipationSetting(
    location = this.location,
    type = this.type,
  )
}
class CourseParticipationOutcomeFactory {
  private var status: CourseStatus = CourseStatus.INCOMPLETE
  private var yearStarted: Year? = null
  private var yearCompleted: Year? = null

  fun withStatus(status: CourseStatus) = apply { this.status = status }
  fun withYearStarted(yearStarted: Year?) = apply { this.yearStarted = yearStarted }
  fun withYearCompleted(yearCompleted: Year?) = apply { this.yearCompleted = yearCompleted }

  fun produce() = CourseParticipationOutcome(
    status = this.status,
    yearStarted = this.yearStarted,
    yearCompleted = this.yearCompleted,
  )
}
