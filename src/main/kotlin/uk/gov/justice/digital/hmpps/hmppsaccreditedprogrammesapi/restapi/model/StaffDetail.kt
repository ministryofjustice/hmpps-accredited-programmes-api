package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import java.util.UUID

data class StaffDetail(
  val staffId: UUID,
  val firstName: String,
  val lastName: String,
  val primaryEmail: String,
  val username: String,
  val type: String,
  val accountType: String?,
)
