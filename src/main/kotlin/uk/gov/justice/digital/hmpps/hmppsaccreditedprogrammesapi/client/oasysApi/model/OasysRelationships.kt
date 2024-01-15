package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRelationships(
  val prevOrCurrentDomesticAbuse: String?,
  val victimOfPartner: String?,
  val victimOfFamily: String?,
  val perpAgainstFamily: String?,
  val perpAgainstPartner: String?,
  val relIssuesDetails: String?,
)
