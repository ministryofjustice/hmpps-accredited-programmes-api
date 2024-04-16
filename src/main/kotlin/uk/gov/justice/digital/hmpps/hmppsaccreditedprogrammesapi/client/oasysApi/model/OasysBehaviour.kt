package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysBehaviour(
  val temperControl: String?,
  val problemSolvingSkills: String?,
  val awarenessOfConsequences: String?,
  val achieveGoals: String?,
  val understandsViewsOfOthers: String?,
  val concreteAbstractThinking: String?,
  val sexualPreOccupation: String?,
  val offenceRelatedSexualInterests: String?,
  val aggressiveControllingBehavour: String?,
  val impulsivity: String?,
)
