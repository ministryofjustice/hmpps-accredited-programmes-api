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
  @get:JsonProperty("dvEvidence") val dvEvidence: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimFormerPartner") val victimFormerPartner: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimFamilyMember") val victimFamilyMember: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("victimOfPartnerFamily") val victimOfPartnerFamily: Boolean? = false,

  @Schema(example = "null", description = "")
  @get:JsonProperty("perpOfPartnerOrFamily") val perpOfPartnerOrFamily: Boolean? = false,

  @Schema(example = "This person has a history of domestic violence", description = "")
  @get:JsonProperty("relIssuesDetails") val relIssuesDetails: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("relCloseFamily") val relCloseFamily: String? = null,

  @Schema(example = "Not in a relationship", description = "")
  @get:JsonProperty("relCurrRelationshipStatus") val relCurrRelationshipStatus: String? = null,

  @Schema(example = "2-Significant problems", description = "")
  @get:JsonProperty("prevCloseRelationships") val prevCloseRelationships: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("emotionalCongruence") val emotionalCongruence: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("relationshipWithPartner") val relationshipWithPartner: String? = null,

  @Schema(example = "No", description = "")
  @get:JsonProperty("prevOrCurrentDomesticAbuse") val prevOrCurrentDomesticAbuse: String? = null,

)
