package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel

@Service
class OasysService(val oasysApiClient: OasysApiClient) {
  fun getOffenceDetail(prisonerId: String): OffenceDetail? {
    val oasysOffenceDetail = getAssessmentId(prisonerId)?.let {
      getOffenceDetail(it)
    }

    if (oasysOffenceDetail == null) {
      throw NotFoundException("No Offence detail found for prisoner id $prisonerId")
    }

    return oasysOffenceDetail.toModel()
  }

  fun getRelationships(prisonerId: String): Relationships? {
    val oasysRelationships = getAssessmentId(prisonerId)?.let {
      getRelationships(it)
    }

    if (oasysRelationships == null) {
      throw NotFoundException("No relationships found for prisoner id $prisonerId")
    }

    return oasysRelationships.toModel()
  }

  fun getAssessmentId(prisonerNumber: String): Long? {
    val assessments = when (val result = oasysApiClient.getAssessments(prisonerNumber)) {
      is ClientResult.Failure -> {
        throw NotFoundException("No assessment found for prisoner id: $prisonerNumber")
      }
      is ClientResult.Success -> AuthorisableActionResult.Success(result.body)
    }

    // get the most recent completed assessment
    val assessment =
      assessments.entity
        .timeline
        .filter { it.status == "COMPLETE" }
        .sortedByDescending { it.completedDate }
        .firstOrNull()

    return if (assessment == null) {
      log.warn("No completed assessment found for prison id $prisonerNumber")
      null
    } else {
      assessment.assessmentPk
    }
  }

  fun getOffenceDetail(assessmentId: Long): OasysOffenceDetail? {
    val offenceDetail = when (val response = oasysApiClient.getOffenceDetail(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return offenceDetail.entity
  }

  fun getRelationships(assessmentId: Long): OasysRelationships? {
    val relationships = when (val response = oasysApiClient.getRelationships(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(response.body)
      }
    }

    return relationships.entity
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
