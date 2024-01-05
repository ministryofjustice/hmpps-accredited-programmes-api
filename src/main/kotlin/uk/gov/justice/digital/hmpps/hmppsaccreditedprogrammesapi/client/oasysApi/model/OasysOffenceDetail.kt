package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffenceDetailWrapper(
  val assessments: List<OasysOffenceDetail>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysOffenceDetail(
  val offenceAnalysis: String?,
  val whatOccurred: List<String>?,
  val recognisesImpact: String?,
  val numberOfOthersInvolved: Int?,
  val othersInvolved: String?,
  val peerGroupInfluences: String?,
  val offenceMotivation: String?,
  val acceptsResponsibilityYesNo: String?,
  val acceptsResponsibility: String?,
  val patternOffending: String?,
)
