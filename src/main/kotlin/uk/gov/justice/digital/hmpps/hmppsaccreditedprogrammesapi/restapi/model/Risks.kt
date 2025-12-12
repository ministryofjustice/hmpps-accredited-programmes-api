package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class Risks(

  var isLegacy: Boolean = true,

  @Schema(example = "45", description = "")
  val ogrsYear1: BigDecimal? = null,

  @Schema(example = "65", description = "")
  val ogrsYear2: BigDecimal? = null,

  @Schema(example = "High", description = "")
  val ogrsRisk: String? = null,

  @Schema(example = "23", description = "")
  val ovpYear1: BigDecimal? = null,

  @Schema(example = "32", description = "")
  val ovpYear2: BigDecimal? = null,

  @Schema(example = "Medium", description = "")
  val ovpRisk: String? = null,

  @Schema(example = "3.45", description = "")
  val rsrScore: BigDecimal? = null,

  @Schema(example = "Medium", description = "")
  val rsrRisk: String? = null,

  @Schema(example = "Low", description = "")
  val ospcScore: String? = null,

  @Schema(example = "High", description = "")
  val ospiScore: String? = null,

  @Schema(example = "Low", description = "")
  val overallRoshLevel: String? = null,

  @Schema(example = "Medium", description = "")
  val riskPrisonersCustody: String? = null,

  @Schema(example = "Medium", description = "")
  val riskStaffCustody: String? = null,

  @Schema(example = "Medium", description = "")
  val riskKnownAdultCustody: String? = null,

  @Schema(example = "Medium", description = "")
  val riskPublicCustody: String? = null,

  @Schema(example = "Medium", description = "")
  val riskChildrenCustody: String? = null,

  @Schema(example = "Medium", description = "")
  val riskStaffCommunity: String? = null,

  @Schema(example = "Medium", description = "")
  val riskKnownAdultCommunity: String? = null,

  @Schema(example = "Medium", description = "")
  val riskPublicCommunity: String? = null,

  @Schema(example = "Medium", description = "")
  val riskChildrenCommunity: String? = null,

  @Schema(example = "Low", description = "")
  val imminentRiskOfViolenceTowardsPartner: String? = null,

  @Schema(example = "Low", description = "")
  val imminentRiskOfViolenceTowardsOthers: String? = null,

  @Schema(example = "null", description = "")
  val alerts: List<Alert>? = null,

  var OGRS4Risks: OGRS4Risks? = null,
)
