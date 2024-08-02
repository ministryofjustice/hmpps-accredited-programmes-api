package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualNeedsAndRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualNeedsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.RiskScores
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
  private val pniNeedsEngine: PniNeedsEngine,
) {
  fun getPniScore(prisonNumber: String): PniScore {
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
      riskScores = buildRiskScores(oasysArnsPredictor, relationships),
    )

    return PniScore(
      prisonNumber = prisonNumber,
      crn = oasysOffendingInfo?.crn,
      assessmentId = assessmentId,
      needsScore = pniNeedsEngine.getOverallNeedsScore(individualNeedsAndRiskScores, prisonNumber),
      riskScores = individualNeedsAndRiskScores.riskScores,
    )
  }

  private fun buildRiskScores(
    oasysArnsPredictor: ArnsScores?,
    relationships: OasysRelationships?,
  ) = RiskScores(
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
