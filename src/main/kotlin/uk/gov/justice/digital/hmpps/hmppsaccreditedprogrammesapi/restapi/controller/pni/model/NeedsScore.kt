package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class NeedsScore(
  @Schema(
    example = "" +
      "{ " +
      "overallNeedsScore=5, " +
      "domainScore=DomainScore(sexDomainScore=1, thinkingDomainScore=2, relationshipDomainScore=1, selfManagementDomainScore=1), " +
      "}",
    description = "",
  )
  @get:JsonProperty("overallNeedsScore") val overallNeedsScore: Int,
  @get:JsonProperty("DomainScore") val domainScore: DomainScore,
)

data class DomainScore(
  @get:JsonProperty("SexDomainScore") val sexDomainScore: SexDomainScore,

  @get:JsonProperty("ThinkingDomainScore") val thinkingDomainScore: ThinkingDomainScore,

  @get:JsonProperty("RelationshipDomainScore") val relationshipDomainScore: RelationshipDomainScore,

  @get:JsonProperty("SelfManagementDomainScore") val selfManagementDomainScore: SelfManagementDomainScore,
)

data class SexDomainScore(
  @get:JsonProperty("overallSexDomainScore") val overAllSexDomainScore: Int,
  @get:JsonProperty("individualSexScores") val individualSexScores: IndividualSexScores,
)

data class ThinkingDomainScore(
  @get:JsonProperty("overallThinkingDomainScore") val overallThinkingDomainScore: Int,
  @get:JsonProperty("individualThinkingScores") val individualThinkingScores: IndividualCognitiveScores,
)

data class RelationshipDomainScore(
  @get:JsonProperty("overallRelationshipDomainScore") val overallRelationshipDomainScore: Int,
  @get:JsonProperty("individualRelationshipScores") val individualRelationshipScores: IndividualRelationshipScores,
)

data class SelfManagementDomainScore(
  @get:JsonProperty("overallSelfManagementDomainScore") val overallSelfManagementDomainScore: Int,
  @get:JsonProperty("individualSelfManagementScores") val individualSelfManagementScores: IndividualSelfManagementScores,
)
