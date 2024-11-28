package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigInteger

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderAllocation(
  val primaryPrisonOffenderManager: PrisonOffenderManager,
  val secondaryPrisonOffenderManager: PrisonOffenderManager,
)

data class PrisonOffenderManager(
  val staffId: BigInteger,
  val name: String,
)
