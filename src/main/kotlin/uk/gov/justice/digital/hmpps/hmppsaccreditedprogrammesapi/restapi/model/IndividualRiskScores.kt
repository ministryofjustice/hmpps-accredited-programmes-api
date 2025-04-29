package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.PniResponse
import java.math.BigDecimal

/**
 *
 * @param ogrs3
 * @param ovp
 * @param ospDc
 * @param ospIic
 * @param rsr
 * @param sara
 */
data class IndividualRiskScores(

  @Schema(example = "1", description = "")
  @Deprecated("ogrs3 is deprecated and will be removed in a future release - use ogrs3Risk instead")
  @get:JsonProperty("ogrs3") val ogrs3: BigDecimal? = null,

  @Schema(example = "Medium", description = "The OGRS risk level")
  @get:JsonProperty("ogrs3Risk") val ogrs3Risk: String? = null,

  @Schema(example = "High", description = "The OVP Risk level")
  @get:JsonProperty("ovpRisk") val ovpRisk: String? = null,

  @Schema(example = "2", description = "")
  @Deprecated("ovp is deprecated and will be removed in a future release - use ovpRisk instead")
  @get:JsonProperty("ovp") val ovp: BigDecimal? = null,

  @Schema(example = "0", description = "")
  @get:JsonProperty("ospDc") val ospDc: String? = null,

  @Schema(example = "1", description = "")
  @get:JsonProperty("ospIic") val ospIic: String? = null,

  @Schema(example = "5", description = "")
  @get:JsonProperty("rsr") val rsr: BigDecimal? = null,

  @Schema(description = "SARA related risk score")
  @get:JsonProperty("sara") val sara: Sara? = null,
) {
  companion object {
    fun from(pniResponse: PniResponse) = IndividualRiskScores(
      ogrs3 = null, // Oasys returns Level rather than numeric value
      ovp = null, // Oasys returns Level rather than numeric value
      ogrs3Risk = pniResponse.assessment?.ogrs3Risk?.type,
      ovpRisk = pniResponse.assessment?.ovpRisk?.type,
      ospDc = pniResponse.assessment?.osp?.cdc?.type,
      ospIic = pniResponse.assessment?.osp?.iiic?.type,
      rsr = pniResponse.assessment?.rsrPercentage?.let { BigDecimal.valueOf(it) },
      sara = Sara.from(pniResponse),
    )
  }
}
