package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
  example = "{\n" +
    "  \"needsScore\": {\n" +
    "    \"overallNeedsScore\": 6,\n" +
    "    \"domainScore\": {\n" +
    "      \"sexDomainScore\": {\n" +
    "        \"overAllSexDomainScore\": 2,\n" +
    "        \"individualSexScores\": {\n" +
    "          \"sexualPreOccupation\": 2,\n" +
    "          \"offenceRelatedSexualInterests\": 1,\n" +
    "          \"emotionalCongruence\": 0\n" +
    "        }\n" +
    "      },\n" +
    "      \"thinkingDomainScore\": {\n" +
    "        \"overallThinkingDomainScore\": 1,\n" +
    "        \"individualThinkingScores\": {\n" +
    "          \"proCriminalAttitudes\": 1,\n" +
    "          \"hostileOrientation\": null\n" +
    "        }\n" +
    "      },\n" +
    "      \"relationshipDomainScore\": {\n" +
    "        \"overallRelationshipDomainScore\": 1,\n" +
    "        \"individualRelationshipScores\": {\n" +
    "          \"curRelCloseFamily\": 0,\n" +
    "          \"prevExpCloseRel\": 2,\n" +
    "          \"easilyInfluenced\": null,\n" +
    "          \"aggressiveControllingBehaviour\": null\n" +
    "        }\n" +
    "      },\n" +
    "      \"selfManagementDomainScore\": {\n" +
    "        \"overallSelfManagementDomainScore\": 2,\n" +
    "        \"individualSelfManagementScores\": {\n" +
    "          \"impulsivity\": null,\n" +
    "          \"temperControl\": null,\n" +
    "          \"problemSolvingSkills\": null,\n" +
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
  @get:JsonProperty("NeedsScore") val needsScore: NeedsScore,
  @get:JsonProperty("RiskScores") val riskScores: RiskScores,
)
