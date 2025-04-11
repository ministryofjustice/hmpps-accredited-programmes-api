package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysDrugDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.getHighestPriorityScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.PrisonerAlertsApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DrugAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysAssessmentDateInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.buildRisks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Service
class OasysService(
  val oasysApiClient: OasysApiClient,
  val prisonerAlertsApiClient: PrisonerAlertsApiClient,
  val auditService: AuditService,
) {
  fun getOffenceDetail(prisonNumber: String): OffenceDetail? {
    auditService.audit(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_OFFENCE_DETAIL.name,
    )

    val oasysOffenceDetail = getAssessmentId(prisonNumber)
      ?.let { getOffenceDetail(it) }
      ?: throw NotFoundException("No Offence detail found for prison number $prisonNumber")

    return oasysOffenceDetail.toModel()
  }

  fun getRelationships(prisonNumber: String): Relationships? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_RELATIONSHIP.name)

    val oasysRelationships = getAssessmentId(prisonNumber)
      ?.let { getRelationships(it) }
      ?: throw NotFoundException("No relationships found for prison number $prisonNumber")

    return oasysRelationships.toModel()
  }

  fun getRoshFull(prisonNumber: String): RoshAnalysis? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_ROSH.name)

    val oasysRoshFull = getAssessmentId(prisonNumber)
      ?.let { getRoshFull(it) }
      ?: throw NotFoundException("No relationships found for prison number $prisonNumber")

    return oasysRoshFull.toModel()
  }

  fun getLifestyle(prisonNumber: String): Lifestyle? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_LIFESTYLE.name)

    val oasysLifestyle = getAssessmentId(prisonNumber)
      ?.let { getLifestyle(it) }
      ?: throw NotFoundException("No lifestyle information found for prison number $prisonNumber")

    return oasysLifestyle.toModel()
  }

  fun getBehaviour(prisonNumber: String): Behaviour? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_BEHAVIOUR.name)

    val oasysBehaviour = getAssessmentId(prisonNumber)
      ?.let { getBehaviour(it) }
      ?: throw NotFoundException("No behaviour information found for prison number $prisonNumber")

    return oasysBehaviour.toModel()
  }

  fun getHealth(prisonNumber: String): Health? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_HEALTH.name)

    val oasysHealth = getAssessmentId(prisonNumber)
      ?.let { getHealth(it) }
      ?: throw NotFoundException("No health information found for prison number $prisonNumber")

    return oasysHealth.toModel()
  }

  fun getAttitude(prisonNumber: String): Attitude? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_ATTITUDE.name)

    val oasysAttitude = getAssessmentId(prisonNumber)
      ?.let { getAttitude(it) }
      ?: throw NotFoundException("No attitude information found for prison number $prisonNumber")

    return oasysAttitude.toModel()
  }

  fun getPsychiatric(prisonNumber: String): Psychiatric? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_PSYCHIATRIC.name)

    val oasysPsychiatric = getAssessmentId(prisonNumber)
      ?.let { getPsychiatric(it) }
      ?: throw NotFoundException("No psychiatric information found for prison number $prisonNumber")

    return oasysPsychiatric.toModel()
  }

  fun getPniCalculation(prisonNumber: String): PniResponse? {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_PNI_SEARCH.name)

    val pniCalculation = when (val response = oasysApiClient.getPniCalculation(prisonNumber)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve PNI calculation for prisonNumber $prisonNumber reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }
    return pniCalculation.entity
  }

  fun getLearningNeeds(prisonNumber: String): LearningNeeds {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_LEARNING.name)
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No learning needs information found for prison number $prisonNumber")

    val oasysLearning = getLearning(assessmentId)
    val oasysAccommodation = getAccommodation(assessmentId)
    return LearningNeeds(oasysAccommodation, oasysLearning)
  }

  fun getRisks(prisonNumber: String): Risks {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_RISKS.name)
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No Risks information found for prison number $prisonNumber")

    val oasysOffendingInfo = getOffendingInfo(assessmentId)
    val oasysRelationships = getRelationships(assessmentId)
    val oasysRoshSummary = getRoshSummary(assessmentId)
    val oasysScoreLevel = oasysRoshSummary?.getHighestPriorityScore()
    val oasysRiskPredictorScores = getRiskPredictors(assessmentId)
    val activeAlerts = getActiveAlerts(prisonNumber)

    return buildRisks(
      oasysOffendingInfo,
      oasysRelationships,
      oasysRoshSummary,
      RiskSummary(oasysScoreLevel?.type),
      oasysRiskPredictorScores,
      activeAlerts,
    )
  }

  fun getAssessmentId(prisonNumber: String): Long? = getAssessmentIdDate(prisonNumber)?.first

  fun getAssessmentIdDate(prisonNumber: String): Pair<Long, LocalDateTime?>? {
    val assessmentTimeline = getAssessments(prisonNumber)

    val assessment = getLatestCompletedLayerThreeAssessment(assessmentTimeline)

    return if (assessment == null) {
      log.warn("No completed assessment found for prison number $prisonNumber")
      null
    } else {
      Pair(assessment.id, assessment.completedAt)
    }
  }

  fun getAssessmentDateInfo(prisonNumber: String): OasysAssessmentDateInfo {
    val assessmentTimeline = getAssessments(prisonNumber)
    val latestCompletedAssessment =
      getLatestCompletedLayerThreeAssessment(assessmentTimeline)

    val mostRecentOpenAssessment = latestCompletedAssessment?.let { latestAssessment ->
      assessmentTimeline.timeline.filter { it.status == "OPEN" }.filter { it.id > latestAssessment.id }
        .maxByOrNull { it.id }
    }

    return OasysAssessmentDateInfo(
      latestCompletedAssessment?.completedAt?.toLocalDate(),
      mostRecentOpenAssessment != null,
    )
  }

  fun getLatestCompletedLayerThreeAssessment(assessment: OasysAssessmentTimeline): Timeline? {
    // get the most recent completed assessment
    return assessment
      .timeline
      .filter { it.status == "COMPLETE" && it.type == "LAYER3" }
      .sortedByDescending { it.completedAt }
      .firstOrNull()
  }

  private fun getAllCompletedLayerThreeAssessments(assessment: OasysAssessmentTimeline): List<Timeline> = assessment
    .timeline
    .filter { it.status == "COMPLETE" && it.type == "LAYER3" }
    .sortedByDescending { it.completedAt }

  fun getAssessmentIdWithCompletedSara(oasysAssessmentTimeline: OasysAssessmentTimeline): Long? {
    val completedLayerThreeAssessments = getAllCompletedLayerThreeAssessments(oasysAssessmentTimeline)
    val latestAssessmentDate = completedLayerThreeAssessments.first().completedAt

    for (assessment in completedLayerThreeAssessments) {
      getRelationships(assessment.id)?.let { relationships ->
        if (relationships.sara != null && isWithinSixWeeks(assessment.completedAt, latestAssessmentDate)) {
          return assessment.id
        }
      }
    }
    log.warn("No completed assessment with SARA data found for prison number ${oasysAssessmentTimeline.prisNumber}")
    return null
  }

  fun isWithinSixWeeks(date1: LocalDateTime?, date2: LocalDateTime?): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(date1, date2)
    return abs(daysBetween) <= 42 // 6 weeks * 7 days/week = 42 days
  }

  fun getAssessments(prisonNumber: String): OasysAssessmentTimeline {
    val assessments = when (val result = oasysApiClient.getAssessments(prisonNumber)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Assessment for $prisonNumber reason ${result.toException().cause}")
        throw NotFoundException("No assessment found for prison number: $prisonNumber")
      }

      is ClientResult.Success -> AuthorisableActionResult.Success(result.body)
    }
    return assessments.entity
  }

  fun getDrugAndAlcoholDetail(prisonNumber: String): DrugAlcoholDetail {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_DRUG_ALCOHOL.name)
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No drug alcohol information found for prison number $prisonNumber")

    val drugDetail = getDrugDetail(assessmentId)
    val alcoholDetail = getAlcoholDetail(assessmentId)
    return DrugAlcoholDetail(
      drug = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysDrugDetail(
        levelOfUseOfMainDrug = drugDetail?.LevelOfUseOfMainDrug,
        drugsMajorActivity = drugDetail?.DrugsMajorActivity,
      ),
      alcohol = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysAlcoholDetail(
        alcoholLinkedToHarm = alcoholDetail?.alcoholLinkedToHarm,
        alcoholIssuesDetails = alcoholDetail?.alcoholIssuesDetails,
        frequencyAndLevel = alcoholDetail?.frequencyAndLevel,
        bingeDrinking = alcoholDetail?.bingeDrinking,
      ),
    )
  }

  fun getActiveAlerts(prisonNumber: String): List<Alert>? {
    val prisonerAlerts = when (val response = prisonerAlertsApiClient.getPrisonerAlertsByPrisonNumber(prisonNumber)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve ActiveAlerts for prisonNumber $prisonNumber reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }
    return prisonerAlerts.entity?.content?.filter { it.isActive }?.sortedByDescending { it.createdAt }
  }

  fun getOffenceDetail(assessmentId: Long): OasysOffenceDetail? = fetchDetail(assessmentId, oasysApiClient::getOffenceDetail, "Offence detail")

  fun getRoshFull(assessmentId: Long): OasysRoshFull? = fetchDetail(assessmentId, oasysApiClient::getRoshFull, "RoshFull")

  fun getRelationships(assessmentId: Long): OasysRelationships? = fetchDetail(assessmentId, oasysApiClient::getRelationships, "Relationships")

  fun getLifestyle(assessmentId: Long): OasysLifestyle? = fetchDetail(assessmentId, oasysApiClient::getLifestyle, "Lifestyle")
  fun getPsychiatric(assessmentId: Long): OasysPsychiatric? = fetchDetail(assessmentId, oasysApiClient::getPsychiatric, "Psychiatric")

  fun getBehaviour(assessmentId: Long): OasysBehaviour? = fetchDetail(assessmentId, oasysApiClient::getBehaviour, "Behaviour")

  fun getHealth(assessmentId: Long): OasysHealth? = fetchDetail(assessmentId, oasysApiClient::getHealth, "Health")

  fun getAttitude(assessmentId: Long): OasysAttitude? = fetchDetail(assessmentId, oasysApiClient::getAttitude, "Attitude")

  fun getLearning(assessmentId: Long): OasysLearning? = fetchDetail(assessmentId, oasysApiClient::getLearning, "Learning")

  fun getAccommodation(assessmentId: Long): OasysAccommodation? = fetchDetail(assessmentId, oasysApiClient::getAccommodation, "Accomodation")

  fun getOffendingInfo(assessmentId: Long): OasysOffendingInfo? = fetchDetail(assessmentId, oasysApiClient::getOffendingInfo, "OffendingInfo")
  fun getRoshSummary(assessmentId: Long): OasysRoshSummary? = fetchDetail(assessmentId, oasysApiClient::getRoshSummary, "RoshSummary")

  fun getRiskPredictors(assessmentId: Long): OasysRiskPredictorScores? = fetchDetail(assessmentId, oasysApiClient::getRiskPredictors, "RiskPredictors")

  fun getDrugDetail(assessmentId: Long): OasysDrugDetail? = fetchDetail(assessmentId, oasysApiClient::getDrugDetail, "DrugDetail")

  fun getAlcoholDetail(assessmentId: Long): OasysAlcoholDetail? = fetchDetail(assessmentId, oasysApiClient::getAlcoholDetail, "AlcoholDetail")

  fun getLDCScore(prisonNumber: String): Int? = getPniCalculation(prisonNumber)?.assessment?.ldc?.subTotal

  fun getOasysPniProgrammePathway(prisonId: String): String = when (getPniCalculation(prisonId)?.pniCalculation?.pni) {
    Type.H -> "HIGH_INTENSITY_BC"
    Type.M -> "MODERATE_INTENSITY_BC"
    Type.A -> "ALTERNATIVE_PATHWAY"
    Type.O -> "MISSING_INFORMATION"
    else -> throw NotFoundException("No PNI data found for prison number $prisonId")
  }

  private inline fun <T> fetchDetail(
    assessmentId: Long,
    fetchFunction: (Long) -> ClientResult<T>,
    entityName: String,
  ): T? {
    val result = when (val response = fetchFunction(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve $entityName data for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }
    return result.entity
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
