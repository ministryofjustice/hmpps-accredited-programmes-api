package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

private const val HIGH_INTENSITY_BC = "HIGH_INTENSITY_BC"

private const val MODERATE_INTENSITY_BC = "MODERATE_INTENSITY_BC"

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
  private val pniNeedsEngine: PniNeedsEngine,
  private val pniRiskEngine: PniRiskEngine,
  private val pniRuleRepository: PniRuleRepository,
  private val pniResultEntityRepository: PNIResultEntityRepository,
  private val personService: PersonService,
  private val objectMapper: ObjectMapper,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun savePni(prisonNumber: String, gender: String?, savePni: Boolean = false, referralId: UUID? = null) {
    getPniScore(prisonNumber, gender, savePni, referralId)
  }

  fun getPniScore(prisonNumber: String, gender: String? = null, savePni: Boolean = false, referralId: UUID? = null): PniScore {
    log.info("Request received to process PNI for prisonNumber $prisonNumber")

    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.PNI.name,
    )

    val oasysAssessmentTimeline = oasysService.getAssessments(prisonNumber)
    val completedAssessment = oasysService.getLatestCompletedLayerThreeAssessment(oasysAssessmentTimeline) ?: throw NotFoundException("No completed assessments found for $prisonNumber")

    val assessmentId = completedAssessment.id

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

    val learning = oasysService.getLearning(assessmentId)

    // SARA result
    val saraResult = buildSaraResult(oasysAssessmentTimeline, relationships, assessmentId)

    // risks
    val oasysOffendingInfo = oasysService.getOffendingInfo(assessmentId)
    val oasysRiskPredictor = oasysService.getRiskPredictors(assessmentId)

    val individualNeedsAndRiskScores = IndividualNeedsAndRiskScores(
      individualNeedsScores = buildNeedsScores(behavior, relationships, attitude, lifestyle, psychiatric),
      individualRiskScores = buildRiskScores(oasysRiskPredictor, oasysOffendingInfo, saraResult),
    )

    val genderOfPerson = getGenderOfPerson(prisonNumber, gender)
    val overallNeedsScore = pniNeedsEngine.getOverallNeedsScore(
      individualNeedsAndRiskScores,
      prisonNumber,
      genderOfPerson,
      learning?.basicSkillsScore?.toInt(),
    )

    log.info("Overall needs score for prisonNumber $prisonNumber is ${overallNeedsScore.overallNeedsScore} classification ${overallNeedsScore.classification} ")

    val overallRiskScore =
      pniRiskEngine.getOverallRiskScore(individualNeedsAndRiskScores.individualRiskScores, genderOfPerson)

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
      pniResultEntityRepository.save(buildEntity(pniScore, completedAssessment, referralId, learning))
    }
    return pniScore
  }

  private fun buildSaraResult(timeline: OasysAssessmentTimeline, oasysRelationships: OasysRelationships?, assessmentId: Long): Sara = if (oasysRelationships?.sara == null) {
    val completedAssessmentIdWithSara = oasysService.getAssessmentIdWithCompletedSara(timeline)
    val relationshipsWithSara = completedAssessmentIdWithSara?.let { oasysService.getRelationships(it) }
    Sara(
      overallResult = getOverallSARAResult(relationshipsWithSara?.sara),
      saraRiskOfViolenceTowardsPartner = relationshipsWithSara?.sara?.imminentRiskOfViolenceTowardsPartner,
      saraRiskOfViolenceTowardsOthers = relationshipsWithSara?.sara?.imminentRiskOfViolenceTowardsOthers,
      saraAssessmentId = completedAssessmentIdWithSara,
    )
  } else {
    Sara(
      overallResult = getOverallSARAResult(oasysRelationships.sara),
      saraRiskOfViolenceTowardsPartner = oasysRelationships.sara.imminentRiskOfViolenceTowardsPartner,
      saraRiskOfViolenceTowardsOthers = oasysRelationships.sara.imminentRiskOfViolenceTowardsOthers,
      saraAssessmentId = assessmentId,
    )
  }

  private fun buildEntity(
    pniScore: PniScore,
    completedAssessment: Timeline?,
    referralId: UUID?,
    learning: OasysLearning?,
  ): PniResultEntity = PniResultEntity(
    crn = pniScore.crn,
    prisonNumber = pniScore.prisonNumber,
    referralId = referralId,
    oasysAssessmentId = completedAssessment?.id,
    oasysAssessmentCompletedDate = completedAssessment?.completedAt,
    needsClassification = pniScore.needsScore.classification,
    riskClassification = pniScore.riskScore.classification,
    overallNeedsScore = pniScore.needsScore.overallNeedsScore,
    programmePathway = pniScore.programmePathway,
    pniValid = pniScore.validationErrors.isEmpty(),
    pniResultJson = objectMapper.writeValueAsString(pniScore),
    pniAssessmentDate = LocalDateTime.now(),
    basicSkillsScore = learning?.basicSkillsScore?.toInt(),
  )

  private fun getProgramPathway(
    overallNeedsScore: NeedsScore,
    overallRiskScore: RiskScore,
    prisonNumber: String,
  ): String {
    if (overallNeedsScore.validate().isNotEmpty()) {
      return "MISSING_INFORMATION"
    }

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
  ): String? = when {
    pniRiskEngine.isHighIntensityBasedOnRiskScores(individualRiskScores) -> HIGH_INTENSITY_BC
    needsClassification == NeedsClassification.LOW_NEED.name &&
      (pniRiskEngine.isHighSara(individualRiskScores) || pniRiskEngine.isMediumSara(individualRiskScores)) -> MODERATE_INTENSITY_BC

    else -> null
  }

  fun getLDC() = oasysService.getLDCScore() > BigDecimal("2.99")

  private fun buildRiskScores(
    oasysRiskPredictorScores: OasysRiskPredictorScores?,
    oasysOffendingInfo: OasysOffendingInfo?,
    sara: Sara,
  ): IndividualRiskScores = IndividualRiskScores(
    ogrs3 = oasysRiskPredictorScores?.groupReconvictionScore?.twoYears?.round(),
    ovp = oasysRiskPredictorScores?.violencePredictorScore?.twoYears?.round(),
    ospIic = oasysOffendingInfo?.ospIICRisk ?: oasysOffendingInfo?.ospIRisk,
    ospDc = oasysOffendingInfo?.ospDCRisk ?: oasysOffendingInfo?.ospCRisk,
    rsr = oasysRiskPredictorScores?.riskOfSeriousRecidivismScore?.percentageScore?.round(),
    sara = sara,
  )

  private fun getOverallSARAResult(sara: uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Sara?): SaraRisk? = SaraRisk.highestRisk(
    SaraRisk.fromString(sara?.imminentRiskOfViolenceTowardsPartner),
    SaraRisk.fromString(sara?.imminentRiskOfViolenceTowardsOthers),
  )

  fun getGenderOfPerson(prisonNumber: String, prisonerGender: String?): String = prisonerGender
    ?: personService.getPerson(prisonNumber)?.gender
    ?: run {
      log.info("Prisoner $prisonNumber is not available in our db, fetching from external service.")
      personService.createOrUpdatePerson(prisonNumber)
      personService.getPerson(prisonNumber)?.gender
    } ?: throw BusinessException("Gender information missing for prisonNumber $prisonNumber. PNI could not be determined")
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

private fun BigDecimal.round(): BigDecimal = this.setScale(2, RoundingMode.HALF_UP)
