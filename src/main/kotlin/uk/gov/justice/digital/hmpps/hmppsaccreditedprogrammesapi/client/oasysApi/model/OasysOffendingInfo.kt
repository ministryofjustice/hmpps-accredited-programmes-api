package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffendingInfo(
  val ospCRisk: String?,
  val ospIRisk: String?,
  val crn: String?,
)
