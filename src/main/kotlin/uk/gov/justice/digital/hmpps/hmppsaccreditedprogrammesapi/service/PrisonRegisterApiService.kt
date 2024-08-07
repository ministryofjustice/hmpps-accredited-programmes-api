package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

@Service
@Transactional
class PrisonRegisterApiService
@Autowired
constructor(
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisonById(prisonId: String): Prison? {
    val prisonDetailsResult = when (val response = prisonRegisterApiClient.getPrison(prisonId)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${response.status} reason ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
    }

    return prisonDetailsResult.entity
  }

  fun getPrisons(prisonIds: List<String>): List<Prison> {
    val prisons = when (val response = prisonRegisterApiClient.getPrisons(prisonIds)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${response.status} reason ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
    }

    return prisons.entity.orEmpty()
  }

  fun getPrisons(): List<Prison> {
    val prisons = when (val response = prisonRegisterApiClient.getPrisons()) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${response.status} reason ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
    }

    return prisons.entity.orEmpty()
  }
}
