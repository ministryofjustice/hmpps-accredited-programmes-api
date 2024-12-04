package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PomType

data class StaffDetail(
  val staffId: Int,
  val firstName: String,
  val lastName: String,
  val primaryEmail: String,
  val username: String,
  val type: PomType,
  val accountType: AccountType,
)
