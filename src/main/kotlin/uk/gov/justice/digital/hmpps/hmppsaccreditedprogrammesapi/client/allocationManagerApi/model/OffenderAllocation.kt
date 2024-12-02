package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocationResponse(
  val primaryPom: PomDetails,
  val secondaryPom: PomDetails
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PomDetails(
  val staffId: Int,
  val name: String
)
