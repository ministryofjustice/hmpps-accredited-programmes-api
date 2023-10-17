package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome as ApiCourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType as ApiCourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationCreate as ApiCreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as ApiCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting as ApiCourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate as ApiCourseParticipationUpdate

fun ApiCreateCourseParticipation.toDomain() =
  CourseParticipation(
    courseName = courseName,
    prisonNumber = prisonNumber,
    courseId = courseId,
    otherCourseName = otherCourseName,
    source = source,
    detail = detail,
    setting = setting?.toDomain(),
    outcome = outcome?.toDomain(),
  )

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseName = courseName,
  courseId = courseId,
  otherCourseName = otherCourseName,
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
  CourseOutcome(
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

fun CourseParticipation.toApi() = ApiCourseParticipation(
  courseName = courseName,
  id = id!!,
  prisonNumber = prisonNumber,
  setting = setting?.toApi(),
  courseId = courseId,
  otherCourseName = otherCourseName,
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
