package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAlcoholDetail(
  val alcoholLinkedToHarm: String?,
  val alcoholIssuesDetails: String?,
  val frequencyAndLevel: String?,
  val bingeDrinking: String?,
)
