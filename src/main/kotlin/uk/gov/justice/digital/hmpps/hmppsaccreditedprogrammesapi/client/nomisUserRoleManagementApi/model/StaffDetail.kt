package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class StaffDetail(
  val staffId: Int,
  val firstName: String,
  val lastName: String,
  val status: String,
  val primaryEmail: String,
  val generalAccount: Account?,
  val adminAccount: Account?,
)

data class Account(
  val username: String,
)
