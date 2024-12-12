package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import java.math.BigInteger

data class StaffDetail(
  val staffId: BigInteger?,
  val firstName: String,
  val lastName: String,
  val primaryEmail: String,
  val username: String,
  val accountType: AccountType,
)
