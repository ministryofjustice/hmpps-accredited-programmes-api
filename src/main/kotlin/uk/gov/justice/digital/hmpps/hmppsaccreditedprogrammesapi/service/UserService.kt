package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.CaseLoad
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

@Service
class UserService(val prisonApiClient: PrisonApiClient) {
  fun getCurrentUsersCaseloads(allCaseloads: Boolean = false): List<CaseLoad> {
    val caseLoads = when (val response = prisonApiClient.getCurrentUserCaseloads(allCaseloads)) {
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.error("Failure to retrieve or parse data for ${getCurrentUser()}  ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        log.debug("Successful - Retrieved case load information for ${getCurrentUser()}")
        AuthorisableActionResult.Success(response.body)
      }
    }
    return caseLoads.entity.orEmpty()
  }

  private fun getCurrentUser() = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER"

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
