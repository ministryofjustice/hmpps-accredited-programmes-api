package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigInteger

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocation(
  val primaryPom: PrisonOffenderManager?,
  val secondaryPom: PrisonOffenderManager?,
)

data class PrisonOffenderManager(
  val staffId: BigInteger,
  val name: String,
)
