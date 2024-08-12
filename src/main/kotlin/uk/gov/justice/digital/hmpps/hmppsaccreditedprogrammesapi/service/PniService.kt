package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PniRuleRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualNeedsAndRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualNeedsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import java.math.BigDecimal
import java.math.RoundingMode

private const val HIGH_INTENSITY_BC = "HIGH_INTENSITY_BC"

private const val MEDIUM_INTENSITY_BC = "MEDIUM_INTENSITY_BC"

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
  private val pniNeedsEngine: PniNeedsEngine,
  private val pniRiskEngine: PniRiskEngine,
  private val pniRuleRepository: PniRuleRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getPniScore(prisonNumber: String, gender: String?): PniScore {
    log.info("Request received to process PNI for prisonNumber $prisonNumber")

    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.PNI.name,
    )

    val assessmentId = oasysService.getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No assessment id found for $prisonNumber")

    // section 6
    val relationships = oasysService.getRelationships(assessmentId)
    // section 7
    val lifestyle = oasysService.getLifestyle(assessmentId)
    // section 10
    val psychiatric = oasysService.getPsychiatric(assessmentId)
    // section 11
    val behavior = oasysService.getBehaviour(assessmentId)
    // section 12
    val attitude = oasysService.getAttitude(assessmentId)

    // risks
    val oasysOffendingInfo = oasysService.getOffendingInfo(assessmentId)
    val oasysArnsPredictor = oasysOffendingInfo?.crn?.let { oasysService.getArnsPredictorSummary(it) }

    val individualNeedsAndRiskScores = IndividualNeedsAndRiskScores(
      individualNeedsScores = buildNeedsScores(behavior, relationships, attitude, lifestyle, psychiatric),
      individualRiskScores = buildRiskScores(oasysArnsPredictor, relationships),
    )

    val overallNeedsScore = pniNeedsEngine.getOverallNeedsScore(individualNeedsAndRiskScores, prisonNumber, gender)

    log.info("Overall needs score for prisonNumber $prisonNumber is ${overallNeedsScore.overallNeedsScore} classification ${overallNeedsScore.classification} ")

    val overallRiskScore =
      pniRiskEngine.getOverallRiskScore(individualNeedsAndRiskScores.individualRiskScores, prisonNumber)

    log.info("Overall risk classification for prisonNumber $prisonNumber is ${overallNeedsScore.classification} ")

    val programmePathway = getProgramPathway(overallNeedsScore, overallRiskScore, prisonNumber)

    return PniScore(
      prisonNumber = prisonNumber,
      crn = oasysOffendingInfo?.crn,
      assessmentId = assessmentId,
      programmePathway = programmePathway,
      needsScore = overallNeedsScore,
      validationErrors = overallNeedsScore.validate(),
      riskScore = overallRiskScore,
    )
  }

  private fun getProgramPathway(
    overallNeedsScore: NeedsScore,
    overallRiskScore: RiskScore,
    prisonNumber: String,
  ): String {
    val programmePathway = getPathwayAfterApplyingExceptionRules(overallNeedsScore.classification, overallRiskScore.individualRiskScores)
      ?: pniRuleRepository.findPniRuleEntityByOverallNeedAndOverallRisk(
        overallNeedsScore.classification,
        overallRiskScore.classification,
      )?.combinedPathway
      ?: throw BusinessException("Programme pathway for $prisonNumber is missing for the combination of needsClassification ${overallNeedsScore.classification} and riskClassification ${overallRiskScore.classification}")

    log.info("Programme pathway for $prisonNumber: ${overallNeedsScore.classification} + ${overallRiskScore.classification}  -> $programmePathway")

    return programmePathway
  }

  private fun getPathwayAfterApplyingExceptionRules(needsClassification: String, individualRiskScores: IndividualRiskScores): String? {
    return when {
      pniRiskEngine.isHighIntensityBasedOnRiskScores(individualRiskScores) -> HIGH_INTENSITY_BC
      needsClassification == NeedsClassification.LOW_NEED.name &&
        (pniRiskEngine.isHighSara(individualRiskScores) || pniRiskEngine.isMediumSara(individualRiskScores)) -> MEDIUM_INTENSITY_BC
      else -> null
    }
  }

  private fun buildRiskScores(
    oasysArnsPredictor: ArnsScores?,
    relationships: OasysRelationships?,
  ) = IndividualRiskScores(
    ogrs3 = oasysArnsPredictor?.groupReconvictionScore?.twoYears?.round(),
    ovp = oasysArnsPredictor?.violencePredictorScore?.twoYears?.round(),
    ospIic = oasysArnsPredictor?.sexualPredictorScore?.ospIndirectImagePercentageScore?.round()
      ?: oasysArnsPredictor?.sexualPredictorScore?.ospIndecentPercentageScore?.round(),
    ospDc = oasysArnsPredictor?.sexualPredictorScore?.ospDirectContactPercentageScore?.round()
      ?: oasysArnsPredictor?.sexualPredictorScore?.ospContactPercentageScore?.round(),
    rsr = oasysArnsPredictor?.riskOfSeriousRecidivismScore?.percentageScore?.round(),
    sara = relationships?.sara?.imminentRiskOfViolenceTowardsPartner,
  )
}

private fun buildNeedsScores(
  behavior: OasysBehaviour?,
  relationships: OasysRelationships?,
  attitude: OasysAttitude?,
  lifestyle: OasysLifestyle?,
  psychiatric: OasysPsychiatric?,
) = IndividualNeedsScores(
  individualSexScores = IndividualSexScores(
    sexualPreOccupation = behavior?.sexualPreOccupation.getScore(),
    offenceRelatedSexualInterests = behavior?.offenceRelatedSexualInterests.getScore(),
    emotionalCongruence = relationships?.emotionalCongruence.getScore(),
  ),

  individualCognitiveScores = IndividualCognitiveScores(
    proCriminalAttitudes = attitude?.proCriminalAttitudes.getScore(),
    hostileOrientation = attitude?.hostileOrientation.getScore(),
  ),
  individualRelationshipScores = IndividualRelationshipScores(
    curRelCloseFamily = relationships?.relCloseFamily.getScore(),
    prevExpCloseRel = relationships?.prevCloseRelationships.getScore(),
    easilyInfluenced = lifestyle?.easilyInfluenced.getScore(),
    aggressiveControllingBehaviour = behavior?.aggressiveControllingBehavour.getScore(),
  ),
  individualSelfManagementScores = IndividualSelfManagementScores(
    impulsivity = behavior?.impulsivity.getScore(),
    temperControl = behavior?.temperControl.getScore(),
    problemSolvingSkills = behavior?.problemSolvingSkills.getScore(),
    difficultiesCoping = psychiatric?.difficultiesCoping.getScore(),
  ),

)

private fun String?.getScore() = this?.trim()?.split("-")?.firstOrNull()?.trim()?.toIntOrNull()

private fun BigDecimal.round(): BigDecimal {
  return this.setScale(2, RoundingMode.HALF_UP)
}
