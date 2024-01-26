package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArnsSummary(
  val overallRiskLevel: String?,
)
