package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param dvEvidence
 * @param victimFormerPartner
 * @param victimFamilyMember
 * @param victimOfPartnerFamily
 * @param perpOfPartnerOrFamily
 * @param relIssuesDetails
 * @param emotionalCongruence
 * @param relCurrRelationshipStatus
 * @param prevCloseRelationships
 */
data class Relationships(

  @Schema(example = "null", description = "")
  @get:JsonProperty("dvEvidence") val dvEvidence: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimFormerPartner") val victimFormerPartner: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimFamilyMember") val victimFamilyMember: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimOfPartnerFamily") val victimOfPartnerFamily: kotlin.Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("perpOfPartnerOrFamily") val perpOfPartnerOrFamily: kotlin.Boolean? = false,

  @Schema(example = "This person has a history of domestic violence", description = "")
  @get:JsonProperty("relIssuesDetails") val relIssuesDetails: kotlin.String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: kotlin.String? = null,

  @Schema(example = "Not in a relationship", description = "")
  @get:JsonProperty("relCurrRelationshipStatus") val relCurrRelationshipStatus: kotlin.String? = null,

  @Schema(example = "2-Significant problems", description = "")
  @get:JsonProperty("prevCloseRelationships") val prevCloseRelationships: kotlin.String? = null,
)
