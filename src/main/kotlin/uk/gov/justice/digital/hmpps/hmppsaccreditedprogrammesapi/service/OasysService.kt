package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel

@Service
class OasysService(val oasysApiClient: OasysApiClient) {
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

  fun getPsychiatric(prisonNumber: String): Psychiatric? {
    val oasysPsychiatric = getAssessmentId(prisonNumber)?.let {
      getPsychiatric(it)
    }

    if (oasysPsychiatric == null) {
      throw NotFoundException("No psychiatric information found for prison number $prisonNumber")
    }

    return oasysPsychiatric.toModel()
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
        .filter { it.status == "COMPLETE" }
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

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
