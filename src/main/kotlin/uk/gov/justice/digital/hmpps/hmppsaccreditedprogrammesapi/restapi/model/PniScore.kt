package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
data class PniScore(
  @Schema(example = "A1234BC", required = true)
  @get:JsonProperty("prisonNumber") val prisonNumber: String,
  @Schema(example = "D602550", required = true)
  @get:JsonProperty("crn") val crn: String?,
  @Schema(example = "2512235167", required = true)
  @get:JsonProperty("assessmentId") val assessmentId: Long,
  @Schema(example = "HIGH_INTENSITY_BC", required = true)
  @get:JsonProperty("programmePathway") val programmePathway: String,
  @Schema(
    example = "{\n" +
      "  \"prisonNumber\": \"A1234BC\",\n" +
      "  \"crn\": \"X739590\",\n" +
      "  \"assessmentId\": 2114584,\n" +
      "  \"needsScore\": {\n" +
      "    \"overallNeedsScore\": 6,\n" +
      "    \"domainScore\": {\n" +
      "      \"sexDomainScore\": {\n" +
      "        \"overAllSexDomainScore\": 2,\n" +
      "        \"individualSexScores\": {\n" +
      "          \"sexualPreOccupation\": 2,\n" +
      "          \"offenceRelatedSexualInterests\": 2,\n" +
      "          \"emotionalCongruence\": 0\n" +
      "        }\n" +
      "      },\n" +
      "      \"thinkingDomainScore\": {\n" +
      "        \"overallThinkingDomainScore\": 1,\n" +
      "        \"individualThinkingScores\": {\n" +
      "          \"proCriminalAttitudes\": 1,\n" +
      "          \"hostileOrientation\": 1\n" +
      "        }\n" +
      "      },\n" +
      "      \"relationshipDomainScore\": {\n" +
      "        \"overallRelationshipDomainScore\": 1,\n" +
      "        \"individualRelationshipScores\": {\n" +
      "          \"curRelCloseFamily\": 0,\n" +
      "          \"prevExpCloseRel\": 2,\n" +
      "          \"easilyInfluenced\": 1,\n" +
      "          \"aggressiveControllingBehaviour\": 1\n" +
      "        }\n" +
      "      },\n" +
      "      \"selfManagementDomainScore\": {\n" +
      "        \"overallSelfManagementDomainScore\": 2,\n" +
      "        \"individualSelfManagementScores\": {\n" +
      "          \"impulsivity\": 1,\n" +
      "          \"temperControl\": 4,\n" +
      "          \"problemSolvingSkills\": 2,\n" +
      "          \"difficultiesCoping\": null\n" +
      "        }\n" +
      "      }\n" +
      "    }\n" +
      "  },\n" +
      "}\n",
  )
  @get:JsonProperty("NeedsScore") val needsScore: NeedsScore,
  @Schema(
    example = "  \"riskScores\": {\n" +
      "    \"ogrs3\": 15.0,\n" +
      "    \"ovp\": 15.0,\n" +
      "    \"ospDc\": 1.07,\n" +
      "    \"ospIic\": 0.11,\n" +
      "    \"rsr\": 1.46,\n" +
      "    \"sara\": \"High\"\n" +
      "  }\n",
  )
  @get:JsonProperty("RiskScore") val riskScore: RiskScore,
  @Schema(example = "['impulsivity is missing ']", required = true)
  @get:JsonProperty("validationErrors") val validationErrors: List<String>,
)
