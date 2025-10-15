package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param offenceDetails
 * @param whereAndWhen
 * @param howDone
 * @param whoVictims
 * @param anyoneElsePresent
 * @param whyDone
 * @param sources
 */
data class RoshAnalysis(

  @Schema(example = "Tax evasion", description = "")
  @get:JsonProperty("offenceDetails") val offenceDetails: String? = null,

  @Schema(example = "at home", description = "")
  @get:JsonProperty("whereAndWhen") val whereAndWhen: String? = null,

  @Schema(example = "false accounting", description = "")
  @get:JsonProperty("howDone") val howDone: String? = null,

  @Schema(example = "hmrc", description = "")
  @get:JsonProperty("whoVictims") val whoVictims: String? = null,

  @Schema(example = "company secretary", description = "")
  @get:JsonProperty("anyoneElsePresent") val anyoneElsePresent: String? = null,

  @Schema(example = "Greed", description = "")
  @get:JsonProperty("whyDone") val whyDone: String? = null,

  @Schema(example = "crown court", description = "")
  @get:JsonProperty("sources") val sources: String? = null,

  @Schema(description = "Any behaviours or incidents that evidence the individual’s ability to cause serious harm and when they happened")
  @get:JsonProperty("identifyBehavioursIncidents") val identifyBehavioursIncidents: String? = null,

  @Schema(description = "An analysis of any current or previous suicide and/or self-harm concerns")
  @get:JsonProperty("analysisSuicideSelfHarm") val analysisSuicideSelfHarm: String? = null,

  @Schema(description = "An analysis of of the circumstances, relevant issues, and needs concerning coping in custody and similar settings")
  @get:JsonProperty("analysisCoping") val analysisCoping: String? = null,

  @Schema(description = "An analysis of any current vulnerabilities")
  @get:JsonProperty("analysisVulnerabilities") val analysisVulnerabilities: String? = null,

  @Schema(description = "An analysis of any current or previous escape and abscond concerns")
  @get:JsonProperty("analysisEscapeAbscond") val analysisEscapeAbscond: String? = null,

  @Schema(description = "An analysis of any aggression, control issues, disruptive behaviour, or breach of trust concerns")
  @get:JsonProperty("analysisControlBehaveTrust") val analysisControlBehaveTrust: String? = null,

  @Schema(description = "An analysis of any patterns related to behaviours or incidents such as: victims, triggers, locations, impact")
  @get:JsonProperty("analysisBehavioursIncidents") val analysisBehavioursIncidents: String? = null,
)
