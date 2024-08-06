package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param proCriminalAttitudes
 * @param motivationToAddressBehaviour
 * @param hostileOrientation
 */
data class Attitude(

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("proCriminalAttitudes") val proCriminalAttitudes: kotlin.String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("motivationToAddressBehaviour") val motivationToAddressBehaviour: kotlin.String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("hostileOrientation") val hostileOrientation: kotlin.String? = null,
)
