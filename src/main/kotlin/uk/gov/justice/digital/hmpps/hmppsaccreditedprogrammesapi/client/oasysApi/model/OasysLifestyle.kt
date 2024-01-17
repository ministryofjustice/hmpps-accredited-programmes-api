package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysLifestyle(
  val regActivitiesEncourageOffending: String?,
  val lifestyleIssuesDetails: String?,
)
