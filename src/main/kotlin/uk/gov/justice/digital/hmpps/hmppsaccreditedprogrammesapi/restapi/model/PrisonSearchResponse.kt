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
  @get:JsonProperty("prisonId") val prisonId: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("prisonName") val prisonName: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("active") val active: Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("male") val male: Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("female") val female: Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("contracted") val contracted: Boolean? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("types") val types: List<PrisonType>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("categories") val categories: List<Category>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("addresses") val addresses: List<Address>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("operators") val operators: List<PrisonOperator>? = null,
)
