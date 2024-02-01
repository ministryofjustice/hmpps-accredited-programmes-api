package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.ArnsApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.NomisAlert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.learningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel

@Service
class OasysService(
  val oasysApiClient: OasysApiClient,
  val arnsApiClient: ArnsApiClient,
  val prisonApiClient: PrisonApiClient,
) {
  fun getOffenceDetail(prisonNumber: String): OffenceDetail? {
    val oasysOffenceDetail = getAssessmentId(prisonNumber)?.let {
      getOffenceDetail(it)
    }

    if (oasysOffenceDetail == null) {
      throw NotFoundException("No Offence detail found for prison number $prisonNumber")
    }

    return oasysOffenceDetail.toModel()
  }

  fun getRelationships(prisonNumber: String): Relationships? {
    val oasysRelationships = getAssessmentId(prisonNumber)?.let {
      getRelationships(it)
    }

    if (oasysRelationships == null) {
      throw NotFoundException("No relationships found for prison number $prisonNumber")
    }

    return oasysRelationships.toModel()
  }

  fun getRoshFull(prisonNumber: String): RoshAnalysis? {
    val oasysRoshFull = getAssessmentId(prisonNumber)?.let {
      getRoshFull(it)
    }

    if (oasysRoshFull == null) {
      throw NotFoundException("No relationships found for prison number $prisonNumber")
    }

    return oasysRoshFull.toModel()
  }

  fun getLifestyle(prisonNumber: String): Lifestyle? {
    val oasysLifestyle = getAssessmentId(prisonNumber)?.let {
      getLifestyle(it)
    }

    if (oasysLifestyle == null) {
      throw NotFoundException("No lifestyle information found for prison number $prisonNumber")
    }

    return oasysLifestyle.toModel()
  }

  fun getBehaviour(prisonNumber: String): Behaviour? {
    val oasysBehaviour = getAssessmentId(prisonNumber)?.let {
      getBehaviour(it)
    }

    if (oasysBehaviour == null) {
      throw NotFoundException("No behaviour information found for prison number $prisonNumber")
    }

    return oasysBehaviour.toModel()
  }

  fun getHealth(prisonNumber: String): Health? {
    val oasysHealth = getAssessmentId(prisonNumber)?.let {
      getHealth(it)
    }

    if (oasysHealth == null) {
      throw NotFoundException("No health information found for prison number $prisonNumber")
    }

    return oasysHealth.toModel()
  }

  fun getAttitude(prisonNumber: String): Attitude? {
    val oasysAttitude = getAssessmentId(prisonNumber)?.let {
      getAttitude(it)
    }

    if (oasysAttitude == null) {
      throw NotFoundException("No attitude information found for prison number $prisonNumber")
    }

    return oasysAttitude.toModel()
  }

  fun getPsychiatric(prisonNumber: String): Psychiatric? {
    val oasysPsychiatric = getAssessmentId(prisonNumber)?.let {
      getPsychiatric(it)
    }

    if (oasysPsychiatric == null) {
      throw NotFoundException("No psychiatric information found for prison number $prisonNumber")
    }

    return oasysPsychiatric.toModel()
  }

  fun getLearningNeeds(prisonNumber: String): LearningNeeds {
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No learning needs information found for prison number $prisonNumber")

    val oasysLearning = getLearning(assessmentId)
    val oasysAccommodation = getAccommodation(assessmentId)
    return learningNeeds(oasysAccommodation, oasysLearning)
  }

  fun getRisks(prisonNumber: String): Risks {
    val assessmentId = getAssessmentId(prisonNumber)
      ?: throw NotFoundException("No Risks information found for prison number $prisonNumber")

    val oasysOffendingInfo = getOffendingInfo(assessmentId)
    val oasysRelationships = getRelationships(assessmentId)
    val oasysRoshSummary = getRoshSummary(assessmentId)
    val oasysArnsSummary = oasysOffendingInfo?.crn?.let { getArnsSummary(it) }
    val oasysArnsPredictor = oasysOffendingInfo?.crn?.let { getArnsPredictorSummary(it) }
    val activeAlerts = getActiveAlerts(prisonNumber)

    return risks(oasysOffendingInfo, oasysRelationships, oasysRoshSummary, oasysArnsSummary, oasysArnsPredictor, activeAlerts)
  }

  fun getAssessmentId(prisonerNumber: String): Long? {
    val assessments = when (val result = oasysApiClient.getAssessments(prisonerNumber)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${result.toException().cause}")

        throw NotFoundException("No assessment found for prison number: $prisonerNumber")
      }

      is ClientResult.Success -> AuthorisableActionResult.Success(result.body)
    }

    // get the most recent completed assessment
    val assessment =
      assessments.entity
        .timeline
        .filter { it.status == "COMPLETE" && it.type == "LAYER3" }
        .sortedByDescending { it.completedAt }
        .firstOrNull()

    return if (assessment == null) {
      log.warn("No completed assessment found for prison number $prisonerNumber")
      null
    } else {
      assessment.id
    }
  }

  fun getOffenceDetail(assessmentId: Long): OasysOffenceDetail? {
    val offenceDetail = when (val response = oasysApiClient.getOffenceDetail(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
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
        log.warn("Failure to retrieve data ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return roshSummary.entity
  }

  private fun getArnsSummary(crn: String): ArnsSummary? {
    val arnsSummary = when (val response = arnsApiClient.getSummary(crn)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return arnsSummary.entity
  }

  private fun getArnsPredictorSummary(crn: String): ArnsScores? {
    val arnsPredictors = when (val response = arnsApiClient.getPredictorsAll(crn)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return arnsPredictors.entity
      ?.filter { it.assessmentStatus == "COMPLETE" }
      ?.sortedByDescending { it.completedDate }
      ?.firstOrNull()
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
