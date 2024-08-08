package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param description
 * @param alertType
 * @param dateCreated Date alert was created.
 */
data class Alert(

  @Schema(example = "risk to children", description = "")
  @get:JsonProperty("description") val description: String? = null,

  @Schema(example = "Sexual Offence", description = "")
  @get:JsonProperty("alertType") val alertType: String? = null,

  @Schema(example = "null", description = "Date alert was created.")
  @get:JsonProperty("dateCreated") val dateCreated: java.time.LocalDate? = null,
)
