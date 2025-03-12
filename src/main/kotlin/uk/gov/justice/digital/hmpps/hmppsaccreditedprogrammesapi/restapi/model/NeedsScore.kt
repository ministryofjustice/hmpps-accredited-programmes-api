package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.PniResponse

data class NeedsScore(
  @Schema(example = "5", required = true)
  @get:JsonProperty("overallNeedsScore") val overallNeedsScore: Int?,
  @Schema(example = "6")
  @get:JsonProperty("basicSkillsScore") val basicSkillsScore: Int?,
  @Schema(example = "High Intensity BC", required = true)
  @get:JsonProperty("classification") val classification: String,
  @Schema(example = "5", required = true)
  @get:JsonProperty("DomainScore") val domainScore: DomainScore,
) {
  fun validate() = listOf(
    domainScore.thinkingDomainScore.isAllValuesPresent(),
    domainScore.relationshipDomainScore.isAllValuesPresent(),
    domainScore.selfManagementDomainScore.isAllValuesPresent(),
  ).flatten()
}

data class DomainScore(
  @Schema(example = "1", required = true)
  @get:JsonProperty("SexDomainScore") val sexDomainScore: SexDomainScore,
  @Schema(example = "2", required = true)
  @get:JsonProperty("ThinkingDomainScore") val thinkingDomainScore: ThinkingDomainScore,
  @Schema(example = "1", required = true)
  @get:JsonProperty("RelationshipDomainScore") val relationshipDomainScore: RelationshipDomainScore,
  @Schema(example = "1", required = true)
  @get:JsonProperty("SelfManagementDomainScore") val selfManagementDomainScore: SelfManagementDomainScore,
) {
  companion object {
    fun from(pniResponse: PniResponse): DomainScore = DomainScore(
      sexDomainScore = SexDomainScore(
        overAllSexDomainScore = pniResponse.pniCalculation?.sexDomain?.score,
        individualSexScores = IndividualSexScores(
          sexualPreOccupation = pniResponse.assessment?.questions?.sexualPreOccupation?.score,
          offenceRelatedSexualInterests = pniResponse.assessment?.questions?.offenceRelatedSexualInterests?.score,
          emotionalCongruence = pniResponse.assessment?.questions?.emotionalCongruence?.score,
        ),
      ),
      thinkingDomainScore = ThinkingDomainScore(
        overallThinkingDomainScore = pniResponse.pniCalculation?.thinkingDomain?.score,
        individualThinkingScores = IndividualCognitiveScores(
          proCriminalAttitudes = pniResponse.assessment?.questions?.proCriminalAttitudes?.score,
          hostileOrientation = pniResponse.assessment?.questions?.hostileOrientation?.score,
        ),
      ),
      relationshipDomainScore = RelationshipDomainScore(
        overallRelationshipDomainScore = pniResponse.pniCalculation?.relationshipDomain?.score,
        individualRelationshipScores = IndividualRelationshipScores(
          curRelCloseFamily = pniResponse.assessment?.questions?.relCloseFamily?.score,
          prevExpCloseRel = pniResponse.assessment?.questions?.prevCloseRelationships?.score,
          easilyInfluenced = pniResponse.assessment?.questions?.easilyInfluenced?.score,
          aggressiveControllingBehaviour = pniResponse.assessment?.questions?.aggressiveControllingBehaviour?.score,
        ),
      ),
      selfManagementDomainScore = SelfManagementDomainScore(
        overallSelfManagementDomainScore = pniResponse.pniCalculation?.selfManagementDomain?.score,
        individualSelfManagementScores = IndividualSelfManagementScores(
          impulsivity = pniResponse.assessment?.questions?.impulsivity?.score,
          temperControl = pniResponse.assessment?.questions?.temperControl?.score,
          problemSolvingSkills = pniResponse.assessment?.questions?.problemSolvingSkills?.score,
          difficultiesCoping = pniResponse.assessment?.questions?.difficultiesCoping?.score,
        ),
      ),
    )
  }
}

data class SexDomainScore(
  @Schema(example = "2", required = true)
  @get:JsonProperty("overallSexDomainScore") val overAllSexDomainScore: Int?,
  @get:JsonProperty("individualSexScores") val individualSexScores: IndividualSexScores,
)

data class ThinkingDomainScore(
  @get:JsonProperty("overallThinkingDomainScore") val overallThinkingDomainScore: Int?,
  @get:JsonProperty("individualThinkingScores") val individualThinkingScores: IndividualCognitiveScores,
) {
  @JsonIgnore
  fun isAllValuesPresent() = mutableListOf<String>().apply {
    if (individualThinkingScores.proCriminalAttitudes == null) {
      add("proCriminalAttitudes in ThinkingDomainScore is null")
    }
    if (individualThinkingScores.hostileOrientation == null) {
      add("hostileOrientation in ThinkingDomainScore is null")
    }
  }
}

data class RelationshipDomainScore(
  @get:JsonProperty("overallRelationshipDomainScore") val overallRelationshipDomainScore: Int?,
  @get:JsonProperty("individualRelationshipScores") val individualRelationshipScores: IndividualRelationshipScores,
) {
  @JsonIgnore
  fun isAllValuesPresent() = mutableListOf<String>().apply {
    if (individualRelationshipScores.curRelCloseFamily == null) {
      add("curRelCloseFamily in RelationshipScores is null")
    }
    if (individualRelationshipScores.prevExpCloseRel == null) {
      add("hostileOrientation in RelationshipScores is null")
    }
    if (individualRelationshipScores.easilyInfluenced == null) {
      add("easilyInfluenced in RelationshipScores is null")
    }
    if (individualRelationshipScores.aggressiveControllingBehaviour == null) {
      add("aggressiveControllingBehaviour in RelationshipScores is null")
    }
  }
}

data class SelfManagementDomainScore(
  @get:JsonProperty("overallSelfManagementDomainScore") val overallSelfManagementDomainScore: Int?,
  @get:JsonProperty("individualSelfManagementScores") val individualSelfManagementScores: IndividualSelfManagementScores,
) {
  @JsonIgnore
  fun isAllValuesPresent() = mutableListOf<String>().apply {
    if (individualSelfManagementScores.impulsivity == null) {
      add("impulsivity in SelfManagementScores is null")
    }
    if (individualSelfManagementScores.temperControl == null) {
      add("temperControl in SelfManagementScores is null")
    }
    if (individualSelfManagementScores.problemSolvingSkills == null) {
      add("problemSolvingSkills in SelfManagementScores is null")
    }
    if (individualSelfManagementScores.difficultiesCoping == null) {
      add("difficultiesCoping in SelfManagementScores is null")
    }
  }
}
