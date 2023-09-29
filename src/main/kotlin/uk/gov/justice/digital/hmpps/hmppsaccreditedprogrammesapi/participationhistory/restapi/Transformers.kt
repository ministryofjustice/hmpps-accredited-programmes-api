package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
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

fun CreateCourseParticipation.toDomain() =
  CourseParticipation(
    prisonNumber = prisonNumber,
    courseId = courseId,
    otherCourseName = otherCourseName,
    source = source,
    setting = setting.toDomain(),
    outcome = outcome.toDomain(),
  )

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseId = courseId,
  otherCourseName = otherCourseName,
  setting = setting.toDomain(),
  outcome = outcome.toDomain(),
)

fun CourseParticipationSettingType.toDomain() = when (this) {
  CourseParticipationSettingType.community -> CourseSetting.COMMUNITY
  CourseParticipationSettingType.custody -> CourseSetting.CUSTODY
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
  CourseSetting.CUSTODY -> CourseParticipationSettingType.custody
  CourseSetting.COMMUNITY -> CourseParticipationSettingType.community
}

fun CourseParticipationOutcome.toDomain() =
  CourseOutcome(
    status = status?.toDomain(),
    detail = detail,
    yearStarted = yearStarted?.let(Year::of),
    yearCompleted = yearCompleted?.let(Year::of),
  )

fun CourseParticipationOutcome.Status.toDomain() = when (this) {
  CourseParticipationOutcome.Status.complete -> CourseStatus.COMPLETE
  CourseParticipationOutcome.Status.incomplete -> CourseStatus.INCOMPLETE
}

fun CourseStatus.toApi() = when (this) {
  CourseStatus.INCOMPLETE -> CourseParticipationOutcome.Status.incomplete
  CourseStatus.COMPLETE -> CourseParticipationOutcome.Status.complete
}

fun CourseParticipation.toApi() = ApiCourseParticipation(
  id = id!!,
  prisonNumber = prisonNumber,
  setting = setting.toApi(),
  courseId = courseId,
  otherCourseName = otherCourseName,
  source = source,
  outcome = with(outcome) {
    CourseParticipationOutcome(
      status = status?.toApi(),
      detail = detail,
      yearStarted = yearStarted?.value,
      yearCompleted = yearCompleted?.value,
    )
  },
  addedBy = createdByUsername,
  createdAt = createdDateTime.format(DateTimeFormatter.ISO_DATE_TIME),
)
