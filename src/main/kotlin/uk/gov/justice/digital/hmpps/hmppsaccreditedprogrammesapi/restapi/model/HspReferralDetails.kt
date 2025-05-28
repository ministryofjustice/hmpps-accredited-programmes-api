package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class HspReferralDetails(

  @Schema(example = "A1234AA", required = true, description = "The prison number of the person who is being referred.")
  @get:JsonProperty("prisonNumber", required = true) val prisonNumber: String,

  @Schema(example = "", required = true, description = "A list of all possible sexual offence details.")
  @get:JsonProperty("selectedOffences", required = true) val selectedOffences: List<SexualOffenceDetails> = emptyList(),

  @Schema(example = "The prisoner meets the requirements", required = false, description = "The overriding reason why the prisoner should be considered suitable for the course.")
  @get:JsonProperty("eligibilityOverrideReason", required = false) val eligibilityOverrideReason: String? = null,
)
