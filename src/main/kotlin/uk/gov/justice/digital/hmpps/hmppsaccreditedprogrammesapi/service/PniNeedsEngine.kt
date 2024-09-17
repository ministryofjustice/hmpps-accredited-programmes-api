package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualNeedsAndRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore
import java.math.BigDecimal

private const val ZERO = 0

@Service
class PniNeedsEngine(
  private val personService: PersonService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  fun getOverallNeedsScore(
    individualNeedsAndRiskScores: IndividualNeedsAndRiskScores,
    prisonNumber: String,
    gender: String?,
    basicSkillsScore: Int?,
  ): NeedsScore {
    val sexDomainScore = getSexDomainScore(individualNeedsAndRiskScores, prisonNumber, gender)
    val individualNeedsScores = individualNeedsAndRiskScores.individualNeedsScores
    val thinkingDomainScore = individualNeedsScores.individualCognitiveScores.overallCognitiveDomainScore()
    val relationshipDomainScore = individualNeedsScores.individualRelationshipScores.overallRelationshipScore()
    val selfManagementDomainScore = individualNeedsScores.individualSelfManagementScores.overallSelfManagementScore()
    val overallNeedsScore =
      listOf(sexDomainScore, thinkingDomainScore, relationshipDomainScore, selfManagementDomainScore).sum()

    return NeedsScore(
      overallNeedsScore = overallNeedsScore,
      classification = getClassification(overallNeedsScore),
      domainScore = DomainScore(
        sexDomainScore = SexDomainScore(
          overAllSexDomainScore = sexDomainScore,
          individualSexScores = individualNeedsScores.individualSexScores,
        ),
        thinkingDomainScore = ThinkingDomainScore(
          overallThinkingDomainScore = thinkingDomainScore,
          individualThinkingScores = individualNeedsScores.individualCognitiveScores,
        ),
        relationshipDomainScore = RelationshipDomainScore(
          overallRelationshipDomainScore = relationshipDomainScore,
          individualRelationshipScores = individualNeedsScores.individualRelationshipScores,
        ),
        selfManagementDomainScore = SelfManagementDomainScore(
          overallSelfManagementDomainScore = selfManagementDomainScore,
          individualSelfManagementScores = individualNeedsScores.individualSelfManagementScores,
        ),
      ),
      basicSkillsScore = basicSkillsScore,
    )
  }

  fun getSexDomainScore(individualNeedsAndRiskScores: IndividualNeedsAndRiskScores, prisonNumber: String, prisonerGender: String?): Int {
    val gender = getGenderOfPrisoner(prisonNumber, prisonerGender)

    val individualSexScores = individualNeedsAndRiskScores.individualNeedsScores.individualSexScores

    if (individualSexScores.isAllValuesPresent()) {
      return individualSexScores.overallSexDomainScore(individualSexScores.totalScore())
    }

    val individualRiskScores = individualNeedsAndRiskScores.individualRiskScores
    if (gender.equals("Male", ignoreCase = true) &&
      (individualRiskScores.ospDc?.let { it > BigDecimal.ZERO } == true || individualRiskScores.ospIic?.let { it > BigDecimal.ZERO } == true)
    ) {
      throw BusinessException("PNI information cannot be computed for $gender prisoner $prisonNumber as ospDC or OspII scores are present but some values of SexDomainScore are null")
    }

    if (gender.equals("Female", ignoreCase = true) && individualSexScores.hasSomeDataPresent()) {
      throw BusinessException("PNI information cannot be computed for $gender prisoner $prisonNumber some SexDomainScore is present but not all")
    }

    return ZERO
  }

  private fun getGenderOfPrisoner(prisonNumber: String, prisonerGender: String?): String {
    return prisonerGender
      ?: personService.getPerson(prisonNumber)?.gender
      ?: run {
        log.info("Prisoner $prisonNumber is not available in our db, fetching from external service.")
        personService.createOrUpdatePerson(prisonNumber)
        personService.getPerson(prisonNumber)?.gender
      } ?: throw BusinessException("Gender information missing for prisonNumber $prisonNumber. PNI could not be determined")
  }
}

private fun getClassification(overallNeedsScore: Int): String {
  return when (overallNeedsScore) {
    in 0..2 -> NeedsClassification.LOW_NEED.name
    in 3..5 -> NeedsClassification.MEDIUM_NEED.name
    in 6..8 -> NeedsClassification.HIGH_NEED.name
    else -> throw BusinessException("Unable to compute classification. Overall needs score is $overallNeedsScore")
  }
}

enum class NeedsClassification {
  LOW_NEED,
  MEDIUM_NEED,
  HIGH_NEED,
}
