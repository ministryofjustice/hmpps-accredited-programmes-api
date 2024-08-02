package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

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
    "  \"riskScores\": {\n" +
    "    \"ogrs3\": 15.0,\n" +
    "    \"ovp\": 15.0,\n" +
    "    \"ospDc\": 1.07,\n" +
    "    \"ospIic\": 0.11,\n" +
    "    \"rsr\": 1.46,\n" +
    "    \"sara\": \"High\"\n" +
    "  }\n" +
    "}\n",
  description = "",
)
data class PniScore(
  @get:JsonProperty("prisonNumber") val prisonNumber: String,
  @get:JsonProperty("crn") val crn: String?,
  @get:JsonProperty("assessmentId") val assessmentId: Long,
  @get:JsonProperty("NeedsScore") val needsScore: NeedsScore,
  @get:JsonProperty("RiskScores") val riskScores: RiskScores,
)
