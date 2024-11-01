package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class BuildingChoicesSearchRequest(

  @Schema(example = "true", required = true, description = "Have they been convicted of a sexual offence?")
  @get:JsonProperty("isConvictedOfSexualOffence", required = true) val isConvictedOfSexualOffence: Boolean,

  @Schema(example = "true", required = true, description = "Are they in a women's prison?")
  @get:JsonProperty("isInAWomensPrison", required = true) val isInAWomensPrison: Boolean,
)
