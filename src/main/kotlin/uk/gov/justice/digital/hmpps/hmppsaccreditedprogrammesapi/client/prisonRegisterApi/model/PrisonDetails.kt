package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonDetails(
  var prisonId: String? = null,
  var prisonName: String? = null,
)
