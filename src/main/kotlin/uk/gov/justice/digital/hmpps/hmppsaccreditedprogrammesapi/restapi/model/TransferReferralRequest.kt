package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param offeringId The id (UUID) of an active offering
 * @param prisonNumber The prison number of the person who is being referred.
 * @param originalReferralId Referral ID of the original referral from which transfer was initiated
 */
data class ReferralCreate(

  @Schema(example = "null", required = true, description = "The id (UUID) of an active offering")
  @get:JsonProperty("offeringId", required = true) val offeringId: UUID,

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

  @Schema(example = "44e3cdab-c996-4234-afe5-a9d8ddb13be8", description = "Referral ID of the original referral from which transfer was initiated")
  @get:JsonProperty("originalReferralId") val originalReferralId: UUID? = null,
)
