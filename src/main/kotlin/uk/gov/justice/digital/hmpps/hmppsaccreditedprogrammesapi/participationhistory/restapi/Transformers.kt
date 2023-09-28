package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as ApiCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate as ApiCourseParticipationUpdate

fun CreateCourseParticipation.toDomain() =
  CourseParticipation(
    prisonNumber = prisonNumber,
    courseId = courseId,
    otherCourseName = otherCourseName,
    source = source,
    setting = setting?.toDomain(),
    outcome = outcome?.toDomain(),
    yearStarted = outcome?.yearStarted?.let(Year::of),
  )

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseId = courseId,
  yearStarted = outcome?.yearStarted?.let(Year::of),
  setting = setting?.toDomain(),
  otherCourseName = otherCourseName,
  outcome = outcome?.toDomain(),
)

fun CourseParticipationSettingType.toDomain() = when (this) {
  CourseParticipationSettingType.community -> CourseSetting.COMMUNITY
  CourseParticipationSettingType.custody -> CourseSetting.CUSTODY
}

fun CourseParticipationSetting.toDomain() = this.type?.toDomain()

fun CourseSetting.toApi() = CourseParticipationSetting(
  type = when (this) {
    CourseSetting.CUSTODY -> CourseParticipationSettingType.custody
    CourseSetting.COMMUNITY -> CourseParticipationSettingType.community
  },
)

fun CourseParticipationOutcome.toDomain() = CourseOutcome(status = status?.toDomain(), detail = detail)

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
  setting = setting?.toApi(),
  courseId = courseId,
  otherCourseName = otherCourseName,
  source = source,
  outcome = outcome?.let {
    CourseParticipationOutcome(
      status = it.status?.toApi(),
      detail = it.detail,
      yearStarted = yearStarted?.value,
    )
  },
)
