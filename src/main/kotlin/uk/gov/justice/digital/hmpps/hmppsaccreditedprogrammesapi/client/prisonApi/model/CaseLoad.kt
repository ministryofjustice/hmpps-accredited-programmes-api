package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class CaseLoad(
  val caseLoadId: String?,
  val description: String?,
  val type: String?,
  val caseloadFunction: String?,
  val currentlyActive: Boolean?,
)
