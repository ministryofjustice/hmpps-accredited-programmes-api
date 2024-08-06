package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param ogrsYear1
 * @param ogrsYear2
 * @param ogrsRisk
 * @param ovpYear1
 * @param ovpYear2
 * @param ovpRisk
 * @param rsrScore
 * @param rsrRisk
 * @param ospcScore
 * @param ospiScore
 * @param overallRoshLevel
 * @param riskPrisonersCustody
 * @param riskStaffCustody
 * @param riskKnownAdultCustody
 * @param riskPublicCustody
 * @param riskChildrenCustody
 * @param riskStaffCommunity
 * @param riskKnownAdultCommunity
 * @param riskPublicCommunity
 * @param riskChildrenCommunity
 * @param imminentRiskOfViolenceTowardsPartner
 * @param imminentRiskOfViolenceTowardsOthers
 * @param alerts
 */
data class Risks(

  @Schema(example = "45", description = "")
  @get:JsonProperty("ogrsYear1") val ogrsYear1: java.math.BigDecimal? = null,

  @Schema(example = "65", description = "")
  @get:JsonProperty("ogrsYear2") val ogrsYear2: java.math.BigDecimal? = null,

  @Schema(example = "High", description = "")
  @get:JsonProperty("ogrsRisk") val ogrsRisk: kotlin.String? = null,

  @Schema(example = "23", description = "")
  @get:JsonProperty("ovpYear1") val ovpYear1: java.math.BigDecimal? = null,

  @Schema(example = "32", description = "")
  @get:JsonProperty("ovpYear2") val ovpYear2: java.math.BigDecimal? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("ovpRisk") val ovpRisk: kotlin.String? = null,

  @Schema(example = "3.45", description = "")
  @get:JsonProperty("rsrScore") val rsrScore: java.math.BigDecimal? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("rsrRisk") val rsrRisk: kotlin.String? = null,

  @Schema(example = "Low", description = "")
  @get:JsonProperty("ospcScore") val ospcScore: kotlin.String? = null,

  @Schema(example = "High", description = "")
  @get:JsonProperty("ospiScore") val ospiScore: kotlin.String? = null,

  @Schema(example = "Low", description = "")
  @get:JsonProperty("overallRoshLevel") val overallRoshLevel: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskPrisonersCustody") val riskPrisonersCustody: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskStaffCustody") val riskStaffCustody: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskKnownAdultCustody") val riskKnownAdultCustody: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskPublicCustody") val riskPublicCustody: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskChildrenCustody") val riskChildrenCustody: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskStaffCommunity") val riskStaffCommunity: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskKnownAdultCommunity") val riskKnownAdultCommunity: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskPublicCommunity") val riskPublicCommunity: kotlin.String? = null,

  @Schema(example = "Medium", description = "")
  @get:JsonProperty("riskChildrenCommunity") val riskChildrenCommunity: kotlin.String? = null,

  @Schema(example = "Low", description = "")
  @get:JsonProperty("imminentRiskOfViolenceTowardsPartner") val imminentRiskOfViolenceTowardsPartner: kotlin.String? = null,

  @Schema(example = "Low", description = "")
  @get:JsonProperty("imminentRiskOfViolenceTowardsOthers") val imminentRiskOfViolenceTowardsOthers: kotlin.String? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("alerts") val alerts: kotlin.collections.List<Alert>? = null,
)
