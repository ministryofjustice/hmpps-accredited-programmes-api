package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year

fun CreateCourseParticipation.toDomain() =
  CourseParticipationHistory(
    prisonNumber = prisonNumber,
    courseId = courseId,
    otherCourseName = otherCourseName,
    setting = setting?.toDomain(),
    outcome = outcome?.toDomain(),
    yearStarted = yearStarted?.let(Year::of),
  )

fun CourseParticipationUpdate.toDomain() = CourseParticipationHistoryUpdate(
  courseId = courseId,
  yearStarted = yearStarted?.let(Year::of),
  setting = setting?.toDomain(),
  otherCourseName = otherCourseName,
  outcome = outcome?.toDomain(),
)

fun CourseSetting.toDomain() = when (this) {
  CourseSetting.community -> uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting.COMMUNITY
  CourseSetting.custody -> uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting.CUSTODY
}

fun uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting.toApi() = when (this) {
  uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting.CUSTODY -> CourseSetting.custody
  uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting.COMMUNITY -> CourseSetting.community
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

fun CourseParticipationHistory.toApi() = CourseParticipation(
  id = id!!,
  prisonNumber = prisonNumber,
  setting = setting?.toApi(),
  courseId = courseId,
  otherCourseName = otherCourseName,
  yearStarted = yearStarted?.value,
  outcome = outcome?.let {
    CourseParticipationOutcome(
      status = it.status?.toApi(),
      detail = it.detail,
    )
  },
)
