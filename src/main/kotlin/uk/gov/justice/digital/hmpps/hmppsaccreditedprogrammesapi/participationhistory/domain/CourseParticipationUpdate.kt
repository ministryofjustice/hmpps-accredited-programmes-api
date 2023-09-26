package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import java.time.Year
import java.util.UUID

data class CourseParticipationUpdate(
  val courseId: UUID? = null,
  val otherCourseName: String?,
  val yearStarted: Year?,
  val setting: CourseSetting?,
  val outcome: CourseOutcome?,
)
