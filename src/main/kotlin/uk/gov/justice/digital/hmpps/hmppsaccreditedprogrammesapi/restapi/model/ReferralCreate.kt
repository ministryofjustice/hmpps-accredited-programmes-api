package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Contextual
import java.util.UUID

/**
 *
 * @param offeringId The id (UUID) of an active offering
 * @param prisonNumber The prison number of the person who is being referred.
 */
data class ReferralCreate(

  @Schema(example = "null", required = true, description = "The id (UUID) of an active offering")
  @Contextual
  @get:JsonProperty("offeringId", required = true) val offeringId: UUID,

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,
)
