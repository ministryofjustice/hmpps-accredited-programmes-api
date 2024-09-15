package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
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
import java.time.LocalDateTime
import java.util.UUID

private const val HIGH_INTENSITY_BC = "HIGH_INTENSITY_BC"

private const val MEDIUM_INTENSITY_BC = "MEDIUM_INTENSITY_BC"

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
  private val pniNeedsEngine: PniNeedsEngine,
  private val pniRiskEngine: PniRiskEngine,
  private val pniRuleRepository: PniRuleRepository,
  private val pniResultEntityRepository: PNIResultEntityRepository,
  private val objectMapper: ObjectMapper,
  private val peopleSearchApiService: PeopleSearchApiService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun savePni(prisonNumber: String, gender: String?, savePni: Boolean = false, referralId: UUID? = null) {
    getPniScore(prisonNumber, gender, savePni, referralId)
  }

  fun getPniScore(prisonNumber: String, gender: String?, savePni: Boolean = false, referralId: UUID? = null): PniScore {
    log.info("Request received to process PNI for prisonNumber $prisonNumber")

    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.PNI.name,
    )

    val assessmentIdDate = oasysService.getAssessmentIdDate(prisonNumber)
      ?: throw NotFoundException("No assessment id found for $prisonNumber")
    val assessmentId = assessmentIdDate.first

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
    val oasysRiskPredictor = oasysService.getRiskPredictors(assessmentId)

    val individualNeedsAndRiskScores = IndividualNeedsAndRiskScores(
      individualNeedsScores = buildNeedsScores(behavior, relationships, attitude, lifestyle, psychiatric),
      individualRiskScores = buildRiskScores(oasysRiskPredictor, relationships),
    )

    val overallNeedsScore = pniNeedsEngine.getOverallNeedsScore(individualNeedsAndRiskScores, prisonNumber, gender)

    log.info("Overall needs score for prisonNumber $prisonNumber is ${overallNeedsScore.overallNeedsScore} classification ${overallNeedsScore.classification} ")

    val overallRiskScore =
      pniRiskEngine.getOverallRiskScore(individualNeedsAndRiskScores.individualRiskScores, prisonNumber)

    log.info("Overall risk classification for prisonNumber $prisonNumber is ${overallNeedsScore.classification} ")

    val programmePathway = getProgramPathway(overallNeedsScore, overallRiskScore, prisonNumber)

    val pniScore = PniScore(
      prisonNumber = prisonNumber,
      crn = oasysOffendingInfo?.crn,
      assessmentId = assessmentId,
      programmePathway = programmePathway,
      needsScore = overallNeedsScore,
      validationErrors = overallNeedsScore.validate(),
      riskScore = overallRiskScore,
    )

    if (savePni) {
      pniResultEntityRepository.save(buildEntity(pniScore, assessmentIdDate, referralId))
    }

    return pniScore
  }

  private fun buildEntity(
    pniScore: PniScore,
    assessmentIdDate: Pair<Long, LocalDateTime?>,
    referralId: UUID?,
  ): PniResultEntity {
    return PniResultEntity(
      crn = pniScore.crn,
      prisonNumber = pniScore.prisonNumber,
      referralId = referralId,
      oasysAssessmentId = assessmentIdDate.first,
      oasysAssessmentCompletedDate = assessmentIdDate.second,
      needsClassification = pniScore.needsScore.classification,
      riskClassification = pniScore.riskScore.classification,
      overallNeedsScore = pniScore.needsScore.overallNeedsScore,
      programmePathway = pniScore.programmePathway,
      pniValid = pniScore.validationErrors.isEmpty(),
      pniResultJson = objectMapper.writeValueAsString(pniScore),
      pniAssessmentDate = LocalDateTime.now(),
    )
  }

  private fun getProgramPathway(
    overallNeedsScore: NeedsScore,
    overallRiskScore: RiskScore,
    prisonNumber: String,
  ): String {
    val programmePathway =
      getPathwayAfterApplyingExceptionRules(overallNeedsScore.classification, overallRiskScore.individualRiskScores)
        ?: pniRuleRepository.findPniRuleEntityByOverallNeedAndOverallRisk(
          overallNeedsScore.classification,
          overallRiskScore.classification,
        )?.combinedPathway
        ?: throw BusinessException("Programme pathway for $prisonNumber is missing for the combination of needsClassification ${overallNeedsScore.classification} and riskClassification ${overallRiskScore.classification}")

    log.info("Programme pathway for $prisonNumber: ${overallNeedsScore.classification} + ${overallRiskScore.classification}  -> $programmePathway")

    return programmePathway
  }

  private fun getPathwayAfterApplyingExceptionRules(
    needsClassification: String,
    individualRiskScores: IndividualRiskScores,
  ): String? {
    return when {
      pniRiskEngine.isHighIntensityBasedOnRiskScores(individualRiskScores) -> HIGH_INTENSITY_BC
      needsClassification == NeedsClassification.LOW_NEED.name &&
        (pniRiskEngine.isHighSara(individualRiskScores) || pniRiskEngine.isMediumSara(individualRiskScores)) -> MEDIUM_INTENSITY_BC

      else -> null
    }
  }

  private fun buildRiskScores(
    oasysRiskPredictorScores: OasysRiskPredictorScores?,
    relationships: OasysRelationships?,
  ) = IndividualRiskScores(
    ogrs3 = oasysRiskPredictorScores?.groupReconvictionScore?.twoYears?.round(),
    ovp = oasysRiskPredictorScores?.violencePredictorScore?.twoYears?.round(),
    ospIic = oasysRiskPredictorScores?.sexualPredictorScore?.ospIndirectImagePercentageScore?.round()
      ?: oasysRiskPredictorScores?.sexualPredictorScore?.ospIndecentPercentageScore?.round(),
    ospDc = oasysRiskPredictorScores?.sexualPredictorScore?.ospDirectContactPercentageScore?.round()
      ?: oasysRiskPredictorScores?.sexualPredictorScore?.ospContactPercentageScore?.round(),
    rsr = oasysRiskPredictorScores?.riskOfSeriousRecidivismScore?.percentageScore?.round(),
    sara = relationships?.sara?.imminentRiskOfViolenceTowardsPartner,
  )

  fun getPniReport(prisonIds: List<String>): String {
    val prisoners = peopleSearchApiService.getPrisoners(prisonIds)

    val pniResults = mutableListOf<PniScore>()
    var failures = mutableMapOf<String, String>()
    prisoners.map {
      try {
        pniResults.add(getPniScore(it.prisonerNumber, it.gender))
      } catch (ex: Exception) {
        failures[it.prisonerNumber] = ex.message.orEmpty()
        log.warn("PNI result could not be computed for ${it.prisonerNumber}")
      }
    }

    val sb = StringBuilder()
    val associateBy = pniResults.groupingBy { it.programmePathway }.eachCount()
    val groupedByProgrammePathway = pniResults.groupBy { it.programmePathway }

    // Process each group and count needs based on overAllSexDomainScore
    groupedByProgrammePathway.forEach { (programmePathway, programmeResults) ->

      sb.append("$programmePathway count ${associateBy[programmePathway]} \n")

      val needCounts = programmeResults.groupingBy {
        when (it.needsScore.domainScore.sexDomainScore.overAllSexDomainScore) {
          0 -> "Low sex Need"
          1 -> "Moderate sex Need"
          2 -> "High sex Need"
          else -> "Unknown"
        }
      }.eachCount()

      needCounts.forEach { (needType, count) ->
        sb.append("\n $needType - $count")
      }
    }
    sb.append("\n Failure count ${failures.size} \n $failures")
    return sb.toString()
  }
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
