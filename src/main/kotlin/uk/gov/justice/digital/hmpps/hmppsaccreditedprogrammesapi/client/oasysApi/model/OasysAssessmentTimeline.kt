package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAssessmentTimeline(
  val probNumber: String?,
  val prisNumber: String?,
  val timeline: List<Timeline>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Timeline(
  val assessmentPk: Long,
  val status: String,
  val assessmentType: String,
  val completedDate: LocalDateTime?,
  val initiationDate: LocalDateTime?,
)
