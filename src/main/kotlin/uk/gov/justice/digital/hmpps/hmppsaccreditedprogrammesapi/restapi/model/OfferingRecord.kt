package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param course The name of the Course to which this Offering applies. This value is only present to help with comprehension. It is not used to match offerings with courses.
 * @param identifier The unique identifier of the Course variant to which this Offering applies. The offering is added to the course having this identifier.
 * @param prisonId The prison id for the prison associated with this Offering. This is usually three capital letters.
 * @param referable
 * @param organisation
 * @param contactEmail The email address of the contact for this offering.
 * @param secondaryContactEmail An optional secondary email address of a contact for this offering.
 */
data class OfferingRecord(

  @Schema(example = "Kaizen", required = true, description = "The name of the Course to which this Offering applies. This value is only present to help with comprehension. It is not used to match offerings with courses.")
  @get:JsonProperty("course", required = true) val course: String,

  @Schema(example = "BNM-IPVO", required = true, description = "The unique identifier of the Course variant to which this Offering applies. The offering is added to the course having this identifier.")
  @get:JsonProperty("identifier", required = true) val identifier: String,

  @Schema(example = "MDI", required = true, description = "The prison id for the prison associated with this Offering. This is usually three capital letters.")
  @get:JsonProperty("prisonId", required = true) val prisonId: String,

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("referable", required = true) val referable: Boolean = true,

  @Schema(example = "null", description = "")
  @get:JsonProperty("organisation") val organisation: String? = null,

  @Schema(example = "ap-admin@digital.justice.gov.uk", description = "The email address of the contact for this offering.")
  @get:JsonProperty("contact email") val contactEmail: String? = null,

  @Schema(example = "ap-admin2@digital.justice.gov.uk", description = "An optional secondary email address of a contact for this offering.")
  @get:JsonProperty("secondary contact email") val secondaryContactEmail: String? = null,
)
