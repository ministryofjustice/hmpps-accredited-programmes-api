package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param organisationId The unique identifier associated with the location hosting the offering. For prisons, this is the PrisonId, which is usually three capital letters.
 * @param contactEmail The email address of a contact for this offering
 * @param referable
 * @param id
 * @param organisationEnabled Describes if a referral can be created with an organisation
 * @param secondaryContactEmail An optional secondary email address of a contact for this offering.
 * @param withdrawn
 */
data class CourseOffering(

  @Schema(example = "MDI", required = true, description = "The unique identifier associated with the location hosting the offering. For prisons, this is the PrisonId, which is usually three capital letters.")
  @get:JsonProperty("organisationId", required = true) val organisationId: String,

  @Schema(example = "ap-admin@digital.justice.gov.uk", required = true, description = "The email address of a contact for this offering")
  @get:JsonProperty("contactEmail", required = true) val contactEmail: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("referable", required = true) val referable: Boolean = true,

  @Schema(example = "null", description = "")
  @get:JsonProperty("id") val id: UUID? = null,

  @Schema(example = "null", description = "Describes if a referral can be created with an organisation")
  @get:JsonProperty("organisationEnabled") val organisationEnabled: Boolean? = null,

  @Schema(example = "ap-admin-2@digital.justice.gov.uk", description = "An optional secondary email address of a contact for this offering.")
  @get:JsonProperty("secondaryContactEmail") val secondaryContactEmail: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("withdrawn") val withdrawn: Boolean? = false,

  @Schema(example = "M", description = "Gender for which course is offered")
  @get:JsonProperty("gender") val gender: Gender?,
)

enum class Gender {
  M,
  F,
}
