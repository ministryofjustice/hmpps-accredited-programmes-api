package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDetail(
  val username: String,
  val staffId: Int,
  val firstName: String,
  val lastName: String,
  val primaryEmail: String,
)
