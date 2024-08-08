package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

/**
 *
 * @param referralId The unique id (UUID) of the new referral.
 */
data class ReferralCreated(

  @Schema(example = "null", required = true, description = "The unique id (UUID) of the new referral.")
  @get:JsonProperty("referralId", required = true) val referralId: UUID,
)
