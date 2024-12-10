package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocationResponse(
  @JsonProperty("primary_pom")
  val primaryPom: PomDetail?,
  @JsonProperty("secondary_pom")
  val secondaryPom: PomDetail?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PomDetail(
  @JsonProperty("staff_id")
  val staffId: Int,
  @JsonProperty("name")
  val name: String,
)