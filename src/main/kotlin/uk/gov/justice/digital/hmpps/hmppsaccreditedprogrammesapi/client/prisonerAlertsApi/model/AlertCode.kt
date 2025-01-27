package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class AlertCode(
  val alertTypeCode: String,
  val code: String,
  val description: String,
  val listSequence: Int,
  val isActive: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val createdAt: LocalDateTime,
  val createdBy: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val modifiedAt: LocalDateTime?,
  val modifiedBy: String?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val deactivatedAt: LocalDateTime?,
  val deactivatedBy: String?,
)
