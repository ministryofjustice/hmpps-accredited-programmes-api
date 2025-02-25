package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore

class NeedsScoreFactory {
  private var overallNeedsScore: Int? = 5
  private var basicSkillsScore: Int? = 6
  private var classification: String = "High Intensity BC"
  private var domainScore: DomainScore = DomainScoreFactory().produce()

  fun withOverallNeedsScore(overallNeedsScore: Int?) = apply { this.overallNeedsScore = overallNeedsScore }
  fun withBasicSkillsScore(basicSkillsScore: Int?) = apply { this.basicSkillsScore = basicSkillsScore }
  fun withClassification(classification: String) = apply { this.classification = classification }
  fun withDomainScore(domainScore: DomainScore) = apply { this.domainScore = domainScore }

  fun produce() = NeedsScore(
    overallNeedsScore = overallNeedsScore,
    basicSkillsScore = basicSkillsScore,
    classification = classification,
    domainScore = domainScore,
  )

  class DomainScoreFactory {
    private var sexDomainScore: SexDomainScore = SexDomainScoreFactory().produce()
    private var thinkingDomainScore: ThinkingDomainScore = ThinkingDomainScoreFactory().produce()
    private var relationshipDomainScore: RelationshipDomainScore = RelationshipDomainScoreFactory().produce()
    private var selfManagementDomainScore: SelfManagementDomainScore = SelfManagementDomainScoreFactory().produce()

    fun withSexDomainScore(sexDomainScore: SexDomainScore) = apply { this.sexDomainScore = sexDomainScore }
    fun withThinkingDomainScore(thinkingDomainScore: ThinkingDomainScore) = apply { this.thinkingDomainScore = thinkingDomainScore }

    fun withRelationshipDomainScore(relationshipDomainScore: RelationshipDomainScore) = apply { this.relationshipDomainScore = relationshipDomainScore }

    fun withSelfManagementDomainScore(selfManagementDomainScore: SelfManagementDomainScore) = apply { this.selfManagementDomainScore = selfManagementDomainScore }

    fun produce() = DomainScore(
      sexDomainScore = sexDomainScore,
      thinkingDomainScore = thinkingDomainScore,
      relationshipDomainScore = relationshipDomainScore,
      selfManagementDomainScore = selfManagementDomainScore,
    )

    class SexDomainScoreFactory {
      private var overAllSexDomainScore: Int? = 2
      private var individualSexScores: IndividualSexScores = IndividualSexScores()

      fun withOverAllSexDomainScore(overAllSexDomainScore: Int?) = apply { this.overAllSexDomainScore = overAllSexDomainScore }

      fun withIndividualSexScores(individualSexScores: IndividualSexScores) = apply { this.individualSexScores = individualSexScores }

      fun produce() = SexDomainScore(
        overAllSexDomainScore = overAllSexDomainScore,
        individualSexScores = individualSexScores,
      )
    }

    class ThinkingDomainScoreFactory {
      private var overallThinkingDomainScore: Int? = 1
      private var individualThinkingScores: IndividualCognitiveScores = IndividualCognitiveScores()

      fun withOverallThinkingDomainScore(overallThinkingDomainScore: Int?) = apply { this.overallThinkingDomainScore = overallThinkingDomainScore }

      fun withIndividualThinkingScores(individualThinkingScores: IndividualCognitiveScores) = apply { this.individualThinkingScores = individualThinkingScores }

      fun produce() = ThinkingDomainScore(
        overallThinkingDomainScore = overallThinkingDomainScore,
        individualThinkingScores = individualThinkingScores,
      )
    }

    class RelationshipDomainScoreFactory {
      private var overallRelationshipDomainScore: Int? = 1
      private var individualRelationshipScores: IndividualRelationshipScores = IndividualRelationshipScores()

      fun withOverallRelationshipDomainScore(overallRelationshipDomainScore: Int?) = apply { this.overallRelationshipDomainScore = overallRelationshipDomainScore }

      fun withIndividualRelationshipScores(individualRelationshipScores: IndividualRelationshipScores) = apply { this.individualRelationshipScores = individualRelationshipScores }

      fun produce() = RelationshipDomainScore(
        overallRelationshipDomainScore = overallRelationshipDomainScore,
        individualRelationshipScores = individualRelationshipScores,
      )
    }

    class SelfManagementDomainScoreFactory {
      private var overallSelfManagementDomainScore: Int? = 1
      private var individualSelfManagementScores: IndividualSelfManagementScores = IndividualSelfManagementScores()

      fun withOverallSelfManagementDomainScore(overallSelfManagementDomainScore: Int?) = apply { this.overallSelfManagementDomainScore = overallSelfManagementDomainScore }

      fun withIndividualSelfManagementScores(individualSelfManagementScores: IndividualSelfManagementScores) = apply { this.individualSelfManagementScores = individualSelfManagementScores }

      fun produce() = SelfManagementDomainScore(
        overallSelfManagementDomainScore = overallSelfManagementDomainScore,
        individualSelfManagementScores = individualSelfManagementScores,
      )
    }
  }
}
