package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetailWrapper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toModel

@Service
class OasysService(val oasysApiClient: OasysApiClient) {
  fun getOffenceDetail(prisonerId: String): OffenceDetail? {
    val oasysOffenceDetail = getAssessmentId(prisonerId)?.let {
      getOffenceDetail(it)
    }

    if (oasysOffenceDetail == null || oasysOffenceDetail.assessments?.isEmpty() == true) {
      throw NotFoundException("No Offence detail found for prisoner id $prisonerId")
    }

    return oasysOffenceDetail.assessments!!.first().toModel()
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

  fun getOffenceDetail(assessmentId: Long): OasysOffenceDetailWrapper? {
    val offenceDetail = when (val oasysOffenceDetailResponse = oasysApiClient.getOffenceDetail(assessmentId)) {
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        AuthorisableActionResult.Success(oasysOffenceDetailResponse.body)
      }
    }

    return offenceDetail.entity
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
