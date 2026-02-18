package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import java.util.UUID

/**
 *
 * @param organisationId The unique identifier associated with the location hosting the offering.
 *                       For prisons, this is the PrisonId, which is usually three capital letters.
 * @param contactEmail The email address of a contact for this offering
 * @param referable
 * @param id
 * @param secondaryContactEmail An optional secondary email address of a contact for this offering.
 * @param withdrawn
 */
data class CourseOffering(

  @field:Schema(example = "MDI", required = true,
    description = """
      The unique identifier associated with the location hosting the offering. 
      For prisons, this is the PrisonId, which is usually three capital letters.""")
  @get:JsonProperty("organisationId", required = true) val organisationId: String,

  @field:Schema(example = "ap-admin@digital.justice.gov.uk", required = true,
    description = "The email address of a contact for this offering")
  @get:JsonProperty("contactEmail", required = true)
  @field:NotBlank(message = "The email address of a contact for this offering shoudld not be blank")
  @field:Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.gov.uk",
    flags = [CASE_INSENSITIVE],
    message = "The email address of a contact should have a valid format <username>@<subdomain>.gov.uk")
  val contactEmail: String,

  @field:Schema(example = "null", required = true, description = "")
  @get:JsonProperty("referable", required = true) val referable: Boolean = true,

  @field:Schema(example = "null", description = "")
  @get:JsonProperty("id") val id: UUID? = null,

  @field:Schema(example = "ap-admin-2@digital.justice.gov.uk",
    description = "An optional secondary email address of a contact for this offering.")
  @get:JsonProperty("secondaryContactEmail")
  @field:Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.gov.uk",
    flags = [CASE_INSENSITIVE],
    message = "The secondary email address of a contact should have a valid format <username>@<subdomain>.gov.uk")
  val secondaryContactEmail: String? = null,

  @field:Schema(example = "null", description = "")
  @get:JsonProperty("withdrawn") val withdrawn: Boolean? = false,

  @field:Schema(example = "M", description = "Gender for which course is offered")
  @get:JsonProperty("gender") val gender: Gender? = null,
)
