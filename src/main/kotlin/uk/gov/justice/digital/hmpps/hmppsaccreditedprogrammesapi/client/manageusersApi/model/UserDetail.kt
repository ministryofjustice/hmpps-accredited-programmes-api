package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageusersApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserDetail(
  val username: String,
  val name: String,
  val active: Boolean,
)
