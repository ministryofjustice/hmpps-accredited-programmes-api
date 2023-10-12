package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import java.util.UUID

data class CourseParticipationUpdate(
  val courseName: String?,
  val courseId: UUID? = null,
  val otherCourseName: String?,
  val source: String?,
  val detail: String?,
  val setting: CourseParticipationSetting? = null,
  val outcome: CourseOutcome? = null,
)
