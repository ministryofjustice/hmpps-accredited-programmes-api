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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.CognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.NeedsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.PNIInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SexScores
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
) {
  fun getPniInfo(prisonNumber: String): PNIInfo {
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

    return PNIInfo(
      needsScores = buildNeedsScores(behavior, relationships, attitude, lifestyle, psychiatric),
      riskScores = buildRiskScores(oasysArnsPredictor, relationships),
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
) = NeedsScores(
  sexScores = SexScores(
    sexualPreOccupation = behavior?.sexualPreOccupation.getScore(),
    offenceRelatedSexualInterests = behavior?.offenceRelatedSexualInterests.getScore(),
    emotionalCongruence = relationships?.emotionalCongruence.getScore(),
  ),

  cognitiveScores = CognitiveScores(
    proCriminalAttitudes = attitude?.proCriminalAttitudes.getScore(),
    hostileOrientation = attitude?.hostileOrientation.getScore(),
  ),
  relationshipScores = RelationshipScores(
    curRelCloseFamily = relationships?.relCurrRelationshipStatus.getScore(),
    prevExpCloseRel = relationships?.prevCloseRelationships.getScore(),
    easilyInfluenced = lifestyle?.easilyInfluenced.getScore(),
    aggressiveControllingBehaviour = behavior?.aggressiveControllingBehavour.getScore(),
  ),
  selfManagementScores = SelfManagementScores(
    impulsivity = behavior?.impulsivity.getScore(),
    temperControl = behavior?.temperControl.getScore(),
    problemSolvingSkills = behavior?.problemSolvingSkills.getScore(),
    difficultiesCoping = psychiatric?.difficultiesCoping.getScore(),
  ),

)

private fun String?.getScore() = this?.trim()?.split("-")?.firstOrNull()?.toIntOrNull()

private fun BigDecimal.round(): BigDecimal {
  return this.setScale(2, RoundingMode.HALF_UP)
}
