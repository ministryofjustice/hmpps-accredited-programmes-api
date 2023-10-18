package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting

data class CourseParticipationUpdate(
  val courseName: String?,
  val source: String?,
  val detail: String?,
  val setting: CourseParticipationSetting? = null,
  val outcome: CourseParticipationOutcome? = null,
)
