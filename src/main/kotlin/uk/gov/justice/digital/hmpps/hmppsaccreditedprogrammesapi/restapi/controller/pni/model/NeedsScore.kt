package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class NeedsScore(
  @Schema(name = "overallNeedsScore", example = "5", required = true)
  @get:JsonProperty("overallNeedsScore") val overallNeedsScore: Int,
  @Schema(name = "classification", example = "High Intensity BC", required = true)
  @get:JsonProperty("classification") val classification: String,
  @Schema(name = "DomainScore", example = "null", required = true)
  @get:JsonProperty("DomainScore") val domainScore: DomainScore,
)

data class DomainScore(
  @Schema(name = "SexDomainScore", example = "null", required = true)
  @get:JsonProperty("SexDomainScore") val sexDomainScore: SexDomainScore,
  @Schema(name = "ThinkingDomainScore", example = "null", required = true)
  @get:JsonProperty("ThinkingDomainScore") val thinkingDomainScore: ThinkingDomainScore,
  @Schema(name = "RelationshipDomainScore", example = "null", required = true)
  @get:JsonProperty("RelationshipDomainScore") val relationshipDomainScore: RelationshipDomainScore,
  @Schema(name = "SelfManagementDomainScore", example = "null", required = true)
  @get:JsonProperty("SelfManagementDomainScore") val selfManagementDomainScore: SelfManagementDomainScore,
)

data class SexDomainScore(
  @Schema(name = "overallSexDomainScore", example = "2", required = true)
  @get:JsonProperty("overallSexDomainScore") val overAllSexDomainScore: Int,
  @Schema(name = "IndividualSexScores", example = "null", required = true)
  @get:JsonProperty("individualSexScores") val individualSexScores: IndividualSexScores,
)

data class ThinkingDomainScore(
  @Schema(example = "1", required = true)
  @get:JsonProperty("overallThinkingDomainScore") val overallThinkingDomainScore: Int,
  @Schema(name = "IndividualThinkingScores", example = "null", required = true)
  @get:JsonProperty("individualThinkingScores") val individualThinkingScores: IndividualCognitiveScores,
)

data class RelationshipDomainScore(
  @Schema(example = "2", required = true)
  @get:JsonProperty("overallRelationshipDomainScore") val overallRelationshipDomainScore: Int,
  @Schema(name = "IndividualRelationshipScores", example = "null", required = true)
  @get:JsonProperty("individualRelationshipScores") val individualRelationshipScores: IndividualRelationshipScores,
)

data class SelfManagementDomainScore(
  @get:JsonProperty("overallSelfManagementDomainScore") val overallSelfManagementDomainScore: Int,
  @get:JsonProperty("individualSelfManagementScores") val individualSelfManagementScores: IndividualSelfManagementScores,
)
