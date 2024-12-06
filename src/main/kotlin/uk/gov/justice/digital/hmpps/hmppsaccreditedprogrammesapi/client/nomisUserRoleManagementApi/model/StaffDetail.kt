package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class StaffDetail(
  val staffId: Int,
  val firstName: String,
  val lastName: String,
  val status: String,
  val primaryEmail: String,
  @JsonProperty("generalAccount")
  val generalAccount: Account?,
  @JsonProperty("adminAccount")
  val adminAccount: Account?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
  @JsonProperty("username")
  val username: String,
)
