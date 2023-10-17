package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import java.util.UUID

data class CourseParticipationUpdate(
  val courseName: String?,
  val courseId: UUID? = null,
  val otherCourseName: String?,
  val source: String?,
  val detail: String?,
  val setting: CourseParticipationSetting? = null,
  val outcome: CourseParticipationOutcome? = null,
)
