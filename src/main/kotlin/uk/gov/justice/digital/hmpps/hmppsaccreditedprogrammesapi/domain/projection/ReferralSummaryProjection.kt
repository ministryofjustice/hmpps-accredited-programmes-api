package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.LocalDateTime
import java.util.*

data class ReferralSummaryProjection(
  val referralId: UUID,
  val courseName: String,
  val audience: String,
  val status: ReferralEntity.ReferralStatus,
  val submittedOn: LocalDateTime?,
  val prisonNumber: String,
)
