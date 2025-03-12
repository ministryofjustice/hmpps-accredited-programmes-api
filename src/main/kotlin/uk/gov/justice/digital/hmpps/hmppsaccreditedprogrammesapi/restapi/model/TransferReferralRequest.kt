package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class TransferReferralRequest(

  @Schema(
    example = "44e3cdab-c996-4234-afe5-a9d8ddb13be8",
    description = "Referral ID of the referral from which transfer was initiated",
  )
  @get:JsonProperty("referralId") val referralId: UUID,

  @Schema(example = "null", required = true, description = "The id (UUID) of an active offering")
  @get:JsonProperty("offeringId", required = true) val offeringId: UUID,

  @Schema(example = "The reason for tranferring the referal is", required = true, description = "Reason for transfer of referral to building choices")
  @get:JsonProperty("transferReason", required = true) val transferReason: String,

)
