package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.ArnsScores
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.getHighestPriorityScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.NomisAlert
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.learningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel
import java.time.LocalDateTime

@Service
class OasysService(
  val oasysApiClient: OasysApiClient,
  val prisonApiClient: PrisonApiClient,
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

  fun getLearningNeeds(prisonNumber: String): LearningNeeds {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_LEARNING.name)
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No learning needs information found for prison number $prisonNumber")

    val oasysLearning = getLearning(assessmentId)
    val oasysAccommodation = getAccommodation(assessmentId)
    return learningNeeds(oasysAccommodation, oasysLearning)
  }

  fun getRisks(prisonNumber: String): Risks {
    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.OASYS_SEARCH_FOR_PERSON_RISKS.name)
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No Risks information found for prison number $prisonNumber")

    val oasysOffendingInfo = getOffendingInfo(assessmentId)
    val oasysRelationships = getRelationships(assessmentId)
    val oasysRoshSummary = getRoshSummary(assessmentId)
    val oasysArnsSummary = oasysRoshSummary?.getHighestPriorityScore()
    val oasysArnsPredictor = getRiskPredictors(assessmentId)
    val activeAlerts = getActiveAlerts(prisonNumber)

    return risks(
      oasysOffendingInfo,
      oasysRelationships,
      oasysRoshSummary,
      RiskSummary(oasysArnsSummary?.type),
      oasysArnsPredictor,
      activeAlerts,
    )
  }

  fun getAssessmentId(prisonNumber: String): Long? {
    return getAssessmentIdDate(prisonNumber)?.first
  }

  fun getAssessmentIdDate(prisonNumber: String): Pair<Long, LocalDateTime?>? {
    val assessmentTimeline = getAssessments(prisonNumber)

    val assessment =
      getLatestCompletedLayerThreeAssessment(assessmentTimeline)

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

  private fun getLatestCompletedLayerThreeAssessment(assessment: OasysAssessmentTimeline): Timeline? {
    // get the most recent completed assessment
    return assessment
      .timeline
      .filter { it.status == "COMPLETE" && it.type == "LAYER3" }
      .sortedByDescending { it.completedAt }
      .firstOrNull()
  }

  private fun getAssessments(prisonNumber: String): OasysAssessmentTimeline {
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

  fun getOffenceDetail(assessmentId: Long): OasysOffenceDetail? {
    val offenceDetail = when (val response = oasysApiClient.getOffenceDetail(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve OffenceDetail for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return offenceDetail.entity
  }

  fun getRoshFull(assessmentId: Long): OasysRoshFull? {
    val roshFull = when (val response = oasysApiClient.getRoshFull(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve RoshFull data for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return roshFull.entity
  }

  fun getRelationships(assessmentId: Long): OasysRelationships? {
    val relationships = when (val response = oasysApiClient.getRelationships(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Relationships data for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return relationships.entity
  }

  fun getLifestyle(assessmentId: Long): OasysLifestyle? {
    val lifestyle = when (val response = oasysApiClient.getLifestyle(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Lifestyle for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return lifestyle.entity
  }

  fun getPsychiatric(assessmentId: Long): OasysPsychiatric? {
    val psychiatric = when (val response = oasysApiClient.getPsychiatric(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Psychiatric for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return psychiatric.entity
  }

  fun getBehaviour(assessmentId: Long): OasysBehaviour? {
    val behaviour = when (val response = oasysApiClient.getBehaviour(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Behaviour for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return behaviour.entity
  }

  fun getHealth(assessmentId: Long): OasysHealth? {
    val health = when (val response = oasysApiClient.getHealth(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Health for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return health.entity
  }

  fun getAttitude(assessmentId: Long): OasysAttitude? {
    val attitude = when (val response = oasysApiClient.getAttitude(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Attitude for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return attitude.entity
  }

  fun getLearning(assessmentId: Long): OasysLearning? {
    val learning = when (val response = oasysApiClient.getLearning(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Learning for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return learning.entity
  }

  fun getAccommodation(assessmentId: Long): OasysAccommodation? {
    val accommodation = when (val response = oasysApiClient.getAccommodation(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve Accommodation for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return accommodation.entity
  }

  fun getOffendingInfo(assessmentId: Long): OasysOffendingInfo? {
    val offendingInfo = when (val response = oasysApiClient.getOffendingInfo(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve OffendingInfo for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return offendingInfo.entity
  }

  fun getActiveAlerts(prisonNumber: String): List<NomisAlert>? {
    val nomisAlerts = when (val response = prisonApiClient.getAlertsByPrisonNumber(prisonNumber)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve ActiveAlerts for prisonNumber $prisonNumber reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }
    return nomisAlerts.entity?.filter { it.active && !it.expired }?.sortedByDescending { it.dateCreated }
  }

  fun getRoshSummary(assessmentId: Long): OasysRoshSummary? {
    val roshSummary = when (val response = oasysApiClient.getRoshSummary(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve RoshSummary data for assessmentId $assessmentId reason ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return roshSummary.entity
  }

  fun getRiskPredictors(assessmentId: Long): ArnsScores? =
    fetchDetail(assessmentId, oasysApiClient::getRiskPredictors, "RiskPredictors")

  fun getDrugDetail(assessmentId: Long): OasysDrugDetail? =
    fetchDetail(assessmentId, oasysApiClient::getDrugDetail, "DrugDetail")

  fun getAlcoholDetail(assessmentId: Long): OasysAlcoholDetail? =
    fetchDetail(assessmentId, oasysApiClient::getAlcoholDetail, "AlcoholDetail")

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
