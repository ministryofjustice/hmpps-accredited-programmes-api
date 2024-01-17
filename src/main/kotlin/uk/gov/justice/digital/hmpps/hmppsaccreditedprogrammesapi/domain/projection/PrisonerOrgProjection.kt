package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.LocalDateTime
import java.util.*

data class PrisonerOrgProjection(
  val prisonNumber: String,
  val organisationId: String,
)
