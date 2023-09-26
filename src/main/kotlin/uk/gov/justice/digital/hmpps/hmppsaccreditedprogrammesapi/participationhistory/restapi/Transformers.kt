package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation as ApiCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate as ApiCourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseSetting as ApiCourseSetting

fun CreateCourseParticipation.toDomain() = CourseParticipation(
  prisonNumber = prisonNumber,
  courseId = courseId,
  otherCourseName = otherCourseName,
  source = source,
  setting = setting?.toDomain(),
  outcome = outcome?.toDomain(),
  yearStarted = yearStarted?.let(Year::of),
)

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseId = courseId,
  yearStarted = yearStarted?.let(Year::of),
  setting = setting?.toDomain(),
  otherCourseName = otherCourseName,
  outcome = outcome?.toDomain(),
)

fun ApiCourseSetting.toDomain() = when (this) {
  ApiCourseSetting.community -> CourseSetting.COMMUNITY
  ApiCourseSetting.custody -> CourseSetting.CUSTODY
}

fun CourseSetting.toApi() = when (this) {
  CourseSetting.CUSTODY -> ApiCourseSetting.custody
  CourseSetting.COMMUNITY -> ApiCourseSetting.community
}

fun CourseParticipationOutcome.toDomain() = CourseOutcome(status = status?.toDomain(), detail = detail)

fun CourseParticipationOutcome.Status.toDomain() = when (this) {
  CourseParticipationOutcome.Status.complete -> CourseStatus.COMPLETE
  CourseParticipationOutcome.Status.deselected -> CourseStatus.DESELECTED
  CourseParticipationOutcome.Status.incomplete -> CourseStatus.INCOMPLETE
}

fun CourseStatus.toApi() = when (this) {
  CourseStatus.INCOMPLETE -> CourseParticipationOutcome.Status.incomplete
  CourseStatus.DESELECTED -> CourseParticipationOutcome.Status.deselected
  CourseStatus.COMPLETE -> CourseParticipationOutcome.Status.complete
}

fun CourseParticipation.toApi() = ApiCourseParticipation(
  id = id!!,
  prisonNumber = prisonNumber,
  setting = setting?.toApi(),
  courseId = courseId,
  otherCourseName = otherCourseName,
  yearStarted = yearStarted?.value,
  source = source,
  outcome = outcome?.let {
    CourseParticipationOutcome(
      status = it.status?.toApi(),
      detail = it.detail,
    )
  },
)
