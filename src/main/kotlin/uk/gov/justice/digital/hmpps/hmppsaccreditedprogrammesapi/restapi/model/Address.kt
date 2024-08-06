package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param addressLine1
 * @param town
 * @param postcode
 * @param country
 * @param addressLine2
 * @param county
 */
data class Address(

  @Schema(example = "Higher Lane", required = true, description = "")
  @get:JsonProperty("addressLine1", required = true) val addressLine1: kotlin.String,

  @Schema(example = "Liverpool", required = true, description = "")
  @get:JsonProperty("town", required = true) val town: kotlin.String,

  @Schema(example = "L9 7LH", required = true, description = "")
  @get:JsonProperty("postcode", required = true) val postcode: kotlin.String,

  @Schema(example = "England", required = true, description = "")
  @get:JsonProperty("country", required = true) val country: kotlin.String,

  @Schema(example = "null", description = "")
  @get:JsonProperty("addressLine2") val addressLine2: kotlin.String? = null,

  @Schema(example = "Merseyside", description = "")
  @get:JsonProperty("county") val county: kotlin.String? = null,
)
