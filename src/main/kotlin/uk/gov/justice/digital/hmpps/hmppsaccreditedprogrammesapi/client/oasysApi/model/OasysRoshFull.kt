package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshFull(
  val currentOffenceDetails: String?,
  val currentWhereAndWhen: String?,
  val currentHowDone: String?,
  val currentWhoVictims: String?,
  val currentAnyoneElsePresent: String?,
  val currentWhyDone: String?,
  val currentSources: String?,
)
