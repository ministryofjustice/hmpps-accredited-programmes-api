package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshFull(
  val currentOffenceDetails: String?,
  val currentWhereAndWhen: String?,
  val currentHowDone: String?,
  val currentWhoVictims: String?,
  val currentAnyoneElsePresent: String?,
  val currentWhyDone: String?,
  val currentSources: String?,
  val identifyBehavioursIncidents: String?,
  @JsonProperty("analysisSuicideSelfharm")
  val analysisSuicideSelfHarm: String?,
  val analysisCoping: String?,
  val analysisVulnerabilities: String?,
  val analysisEscapeAbscond: String?,
  val analysisControlBehaveTrust: String?,
  val analysisBehavioursIncidents: String?,

)
