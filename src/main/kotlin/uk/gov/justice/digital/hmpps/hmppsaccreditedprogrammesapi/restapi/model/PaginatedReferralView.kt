package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param content
 * @param totalPages
 * @param totalElements
 * @param pageSize
 * @param pageNumber
 * @param pageIsEmpty
 */
data class PaginatedReferralView(

  @Schema(example = "null", description = "")
  @get:JsonProperty("content") val content: kotlin.collections.List<ReferralView>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("totalPages") val totalPages: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("totalElements") val totalElements: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("pageSize") val pageSize: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("pageNumber") val pageNumber: kotlin.Int? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("pageIsEmpty") val pageIsEmpty: kotlin.Boolean? = null,
)
