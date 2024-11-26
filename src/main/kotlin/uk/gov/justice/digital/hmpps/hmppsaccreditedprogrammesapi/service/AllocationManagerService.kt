package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.AllocationManagerApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.OffenderAllocation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

@Service
@Transactional
class AllocationManagerService(val allocationManagerApiClient: AllocationManagerApiClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getOffenderAllocation(prisonNumber: String): OffenderAllocation? {
    val offenderAllocation = when (val response = allocationManagerApiClient.getPomDetails(prisonNumber)) {
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.error("Failure to retrieve POM information $prisonNumber  ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        log.debug("Retrieved POM information for $prisonNumber")
        AuthorisableActionResult.Success(response.body)
      }
    }
    return offenderAllocation.entity
  }
}
