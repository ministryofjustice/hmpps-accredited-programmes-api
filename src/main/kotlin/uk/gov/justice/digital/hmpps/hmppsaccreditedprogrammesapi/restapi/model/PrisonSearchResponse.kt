package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param prisonId
 * @param prisonName
 * @param active
 * @param male
 * @param female
 * @param contracted
 * @param types
 * @param categories
 * @param addresses
 * @param operators
 */
data class PrisonSearchResponse(

  @Schema(example = "null", description = "")
  @get:JsonProperty("prisonId") val prisonId: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("prisonName") val prisonName: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("active") val active: kotlin.Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("male") val male: kotlin.Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("female") val female: kotlin.Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("contracted") val contracted: kotlin.Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("types") val types: kotlin.collections.List<PrisonType>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("categories") val categories: kotlin.collections.List<Category>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("addresses") val addresses: kotlin.collections.List<Address>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("operators") val operators: kotlin.collections.List<PrisonOperator>? = null,
)
