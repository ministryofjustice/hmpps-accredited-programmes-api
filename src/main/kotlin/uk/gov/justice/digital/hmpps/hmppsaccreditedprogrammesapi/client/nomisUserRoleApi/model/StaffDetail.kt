package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleApi.model

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
  val username: String,
)
