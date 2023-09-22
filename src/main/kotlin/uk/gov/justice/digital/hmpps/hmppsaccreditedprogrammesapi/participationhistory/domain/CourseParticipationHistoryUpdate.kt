package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import java.util.UUID

data class CourseParticipationHistoryUpdate(
  val courseId: UUID? = null,
  val otherCourseName: String?,
  val setting: CourseParticipationSetting,
  val outcome: CourseOutcome,
)
