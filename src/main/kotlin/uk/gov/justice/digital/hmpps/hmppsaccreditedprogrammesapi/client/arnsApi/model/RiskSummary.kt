package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RiskSummary(
  val overallRiskLevel: String?,
)
