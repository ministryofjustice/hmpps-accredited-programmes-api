package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import java.util.UUID

data class CourseParticipationUpdate(
  val courseId: UUID? = null,
  val otherCourseName: String?,
  val source: String?,
  var detail: String?,
  val setting: CourseParticipationSetting,
  val outcome: CourseOutcome,
)
