package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class HspReferralCreate(

  @Schema(example = "550e8400-e29b-41d4-a716-446655440000", required = true, description = "The id (UUID) of the HSP offering")
  @get:JsonProperty("offeringId", required = true) val offeringId: UUID,

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

  @Schema(example = "[\"dd69bdbe-a61d-45e5-bb20-e52af4a0ac83\",\"550e8400-e29b-41d4-a716-446655440000\"]", required = true, description = "The list of IDs of the selected offences.")
  @get:JsonProperty("selectedOffences", required = true) val selectedOffences: List<UUID> = emptyList(),

  @Schema(example = "The prisoner meets the requirements", required = false, description = "The overriding reason why the prisoner should be considered suitable for the course.")
  @get:JsonProperty("eligibilityOverrideReason", required = false) val eligibilityOverrideReason: String? = null,
)
