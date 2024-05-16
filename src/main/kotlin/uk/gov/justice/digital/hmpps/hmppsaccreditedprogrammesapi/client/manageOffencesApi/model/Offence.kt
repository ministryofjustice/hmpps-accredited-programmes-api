package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Offence(
  val id: Long,
  val code: String,
  val description: String? = null,
  val offenceType: String? = null,
  val revisionId: Int,
  val startDate: LocalDate,
  val endDate: LocalDate? = null,
  val homeOfficeStatsCode: String? = null,
  val homeOfficeDescription: String? = null,
  val changedDate: LocalDateTime,
  val loadDate: LocalDateTime? = null,
  val schedules: List<LinkedScheduleDetails>? = null,
  val isChild: Boolean = false,
  val parentOffenceId: Long? = null,
  val childOffenceIds: List<Long>? = null,
  val legislation: String? = null,
  val maxPeriodIsLife: Boolean? = null,
  val maxPeriodOfIndictmentYears: Int? = null,
  val custodialIndicator: CustodialIndicator? = null,
)

data class LinkedScheduleDetails(
  val id: Long,
  val act: String,
  val code: String,
  val url: String? = null,
  val partNumber: Int,
  val paragraphNumber: String? = null,
  val paragraphTitle: String? = null,
  val lineReference: String? = null,
  val legislationText: String? = null,
)

enum class CustodialIndicator {
  @JsonProperty("Y")
  YES,

  @JsonProperty("N")
  NO,

  @JsonProperty("E")
  EITHER,
}
