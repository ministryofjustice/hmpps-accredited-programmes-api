package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshSummary(
  val riskPrisonersCustody: String?,
  val riskStaffCustody: String?,
  val riskStaffCommunity: String?,
  val riskKnownAdultCustody: String?,
  val riskKnownAdultCommunity: String?,
  val riskPublicCustody: String?,
  val riskPublicCommunity: String?,
  val riskChildrenCustody: String?,
  val riskChildrenCommunity: String?,
)
