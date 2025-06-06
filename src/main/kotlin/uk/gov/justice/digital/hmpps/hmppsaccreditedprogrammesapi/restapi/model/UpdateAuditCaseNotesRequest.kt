package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateAuditCaseNotesRequest(

  @Schema(
    example = "44e3cdab-c996-4234-afe5-a9d8ddb13be8",
    description = "Referral ID of the referral from which transfer was initiated",
  )
  @get:JsonProperty("referralId") val referralId: UUID,

  @Schema(example = "The referrer who made whose name should appear on the case notes", required = true, description = "Username of the person who initiated the incorrect status update")
  @get:JsonProperty("referrerUsername", required = true) val referrerUsername: String,

)
