package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocation(
  val primaryPom: Pom,
  val secondaryPom: Pom,
)

data class Pom(
  val staffId: Int,
  val name: String,
)
