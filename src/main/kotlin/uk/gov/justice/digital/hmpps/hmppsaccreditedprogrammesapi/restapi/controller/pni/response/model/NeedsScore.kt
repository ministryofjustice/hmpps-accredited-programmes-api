package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model

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
  @get:JsonProperty("sexDomainScore") val sexDomainScore: Int,

  @get:JsonProperty("thinkingDomainScore") val thinkingDomainScore: Int,

  @get:JsonProperty("relationshipDomainScore") val relationshipDomainScore: Int,

  @get:JsonProperty("selfManagementDomainScore") val selfManagementDomainScore: Int,
)
