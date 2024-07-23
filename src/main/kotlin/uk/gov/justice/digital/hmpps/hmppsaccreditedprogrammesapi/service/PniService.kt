package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.CognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.NeedsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.PNIInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.RiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model.SexScores
import java.math.BigDecimal

class PniService(
  private val oasysService: OasysService
) {
  fun getPniInfo(prisonNumber: String): PNIInfo? {

    val assessmentId = oasysService.getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No assessment id found for $prisonNumber")



    // section 6
    val relationships = oasysService.getRelationships(assessmentId)

    // section 7
    val lifestyle = oasysService.getLifestyle(assessmentId)

    //section 10
    val psychiatric = oasysService.getPsychiatric(assessmentId)

    // section 11
    val behavior = oasysService.getBehaviour(assessmentId)

    // section 12
    val attitude = oasysService.getAttitude(assessmentId)

    // risks
    val risks = oasysService.getRisks(prisonNumber)

    return PNIInfo(
      needsScores = buildNeedsScores(behavior, relationships, attitude, lifestyle, psychiatric),
      riskScores = RiskScores(
        ogrs3 = risks.ogrsYear1,
        ovp = risks.ovpYear1,
        ospDc = risks.ospcScore,
        ospIic = risks.ospiScore,
        rsr = risks.rsrScore,
        sara = risks.,
      ),

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
}