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
  @get:JsonProperty("status", required = true) val status: Int,

  @Schema(example = "null", description = "")
  @get:JsonProperty("errorCode") val errorCode: Int? = null,

  @Schema(example = "Not found", description = "")
  @get:JsonProperty("userMessage") val userMessage: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("developerMessage") val developerMessage: String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("moreInfo") val moreInfo: String? = null,
)
