package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param status
 * @param errorCode
 * @param userMessage
 * @param developerMessage
 * @param moreInfo
 */
data class ErrorResponse(

  @Schema(example = "404", required = true, description = "")
  @get:JsonProperty("status", required = true) val status: kotlin.Int,

  @Schema(example = "null", description = "")
  @get:JsonProperty("errorCode") val errorCode: kotlin.Int? = null,

  @Schema(example = "Not found", description = "")
  @get:JsonProperty("userMessage") val userMessage: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("developerMessage") val developerMessage: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("moreInfo") val moreInfo: kotlin.String? = null,
)
