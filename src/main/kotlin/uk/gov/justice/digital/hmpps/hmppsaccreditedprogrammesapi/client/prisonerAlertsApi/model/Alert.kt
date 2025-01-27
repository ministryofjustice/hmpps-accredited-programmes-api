package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Alert(
  val alertUuid: UUID,
  val prisonNumber: String,
  val alertCode: AlertCodeSummary,
  val description: String?,
  val authorisedBy: String?,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val activeFrom: LocalDate,
  @JsonFormat(pattern = "yyyy-MM-dd")
  val activeTo: LocalDate?,
  val isActive: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val createdAt: LocalDateTime,
  val createdBy: String,
  val createdByDisplayName: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val lastModifiedAt: LocalDateTime?,
  val lastModifiedBy: String?,
  val lastModifiedByDisplayName: String?,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val activeToLastSetAt: LocalDateTime?,
  val activeToLastSetBy: String?,
  val activeToLastSetByDisplayName: String?,
)
