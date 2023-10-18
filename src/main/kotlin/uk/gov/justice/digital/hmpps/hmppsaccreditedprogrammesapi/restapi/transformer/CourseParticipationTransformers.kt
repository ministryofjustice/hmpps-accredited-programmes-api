package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseParticipationUpdate
import java.time.Year
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as ApiCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationCreate as ApiCreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome as ApiCourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting as ApiCourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType as ApiCourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate as ApiCourseParticipationUpdate

fun ApiCreateCourseParticipation.toDomain() =
  CourseParticipationEntity(
    courseName = courseName,
    prisonNumber = prisonNumber,
    source = source,
    detail = detail,
    setting = setting?.toDomain(),
    outcome = outcome?.toDomain(),
  )

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseName = courseName,
  source = source,
  detail = detail,
  setting = setting?.toDomain(),
  outcome = outcome?.toDomain(),
)

fun ApiCourseParticipationSettingType.toDomain() = when (this) {
  ApiCourseParticipationSettingType.community -> CourseSetting.COMMUNITY
  ApiCourseParticipationSettingType.custody -> CourseSetting.CUSTODY
}

fun ApiCourseParticipationSetting.toDomain() = CourseParticipationSetting(
  type = type.toDomain(),
  location = location,
)

fun CourseParticipationSetting.toApi() = ApiCourseParticipationSetting(
  type = type.toApi(),
  location = location,
)

fun CourseSetting.toApi() = when (this) {
  CourseSetting.CUSTODY -> ApiCourseParticipationSettingType.custody
  CourseSetting.COMMUNITY -> ApiCourseParticipationSettingType.community
}

fun ApiCourseParticipationOutcome.toDomain() =
  CourseParticipationOutcome(
    status = status.toDomain(),
    yearStarted = yearStarted?.let(Year::of),
    yearCompleted = yearCompleted?.let(Year::of),
  )

fun ApiCourseParticipationOutcome.Status.toDomain() = when (this) {
  ApiCourseParticipationOutcome.Status.complete -> CourseStatus.COMPLETE
  ApiCourseParticipationOutcome.Status.incomplete -> CourseStatus.INCOMPLETE
}

fun CourseStatus.toApi() = when (this) {
  CourseStatus.INCOMPLETE -> ApiCourseParticipationOutcome.Status.incomplete
  CourseStatus.COMPLETE -> ApiCourseParticipationOutcome.Status.complete
}

fun CourseParticipationEntity.toApi() = ApiCourseParticipation(
  courseName = courseName,
  id = id!!,
  prisonNumber = prisonNumber,
  setting = setting?.toApi(),
  source = source,
  detail = detail,
  outcome = outcome?.let {
    ApiCourseParticipationOutcome(
      status = it.status.toApi(),
      yearStarted = it.yearStarted?.value,
      yearCompleted = it.yearCompleted?.value,
    )
  },
  addedBy = createdByUsername,
  createdAt = createdDateTime.format(DateTimeFormatter.ISO_DATE_TIME),
)
