package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocationResponse(
  @JsonProperty("primary_pom")
  val primaryPom: PomDetails?,
  @JsonProperty("secondary_pom")
  val secondaryPom: PomDetails?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PomDetails(
  @JsonProperty("staff_id")
  val staffId: Int,
  @JsonProperty("name")
  val name: String,
)
