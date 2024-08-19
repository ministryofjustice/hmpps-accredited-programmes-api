package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysRoshSummary(
  val riskPrisonersCustody: ScoreLevel?,
  val riskStaffCustody: ScoreLevel?,
  val riskStaffCommunity: ScoreLevel?,
  val riskKnownAdultCustody: ScoreLevel?,
  val riskKnownAdultCommunity: ScoreLevel?,
  val riskPublicCustody: ScoreLevel?,
  val riskPublicCommunity: ScoreLevel?,
  val riskChildrenCustody: ScoreLevel?,
  val riskChildrenCommunity: ScoreLevel?,
)

enum class ScoreLevel(val type: String, val priority: Int) {
  LOW("Low", 1),
  MEDIUM("Medium", 2),
  HIGH("High", 3),
  VERY_HIGH("Very High", 4),
  NOT_APPLICABLE("Not Applicable", 0),
  ;

  @JsonCreator
  fun fromString(value: String): ScoreLevel? {
    return entries.find { it.type.equals(value, ignoreCase = true) }
  }

  @JsonValue
  fun toValue(): String {
    return this.type
  }
}

fun OasysRoshSummary.getHighestPriorityScore(): ScoreLevel? {
  return listOfNotNull(
    riskPrisonersCustody,
    riskStaffCustody,
    riskStaffCommunity,
    riskKnownAdultCustody,
    riskKnownAdultCommunity,
    riskPublicCustody,
    riskPublicCommunity,
    riskChildrenCustody,
    riskChildrenCommunity,
  ).maxByOrNull { it.priority }
}
