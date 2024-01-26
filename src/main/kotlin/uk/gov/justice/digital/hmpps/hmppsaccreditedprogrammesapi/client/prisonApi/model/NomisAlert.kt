package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class NomisAlert(
  val alertType: String,
  val alertTypeDescription: String,
  val alertCode: String,
  val alertCodeDescription: String,
  val expired: Boolean,
  val active: Boolean,
  val dateCreated: LocalDate,
  val comment: String?,
)
