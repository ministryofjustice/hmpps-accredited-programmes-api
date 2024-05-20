package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Offence(
  val code: String,
  val description: String? = null,
  val legislation: String? = null,
)
