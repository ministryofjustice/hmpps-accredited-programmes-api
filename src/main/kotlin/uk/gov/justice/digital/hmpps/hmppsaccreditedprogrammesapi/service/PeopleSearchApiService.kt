package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonerSearchRequest

@Service
@Transactional
class PeopleSearchApiService
@Autowired
constructor(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisoners(prisonerNumbers: List<String>): List<Prisoner> {
    val prisoners = when (val response = prisonerSearchApiClient.getPrisonersByPrisonNumbers(prisonerNumbers)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${response.status}")
        AuthorisableActionResult.Success(emptyList())
      }
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data for prisonerNumbers $prisonerNumbers Reason ${response.toException().message}")
        AuthorisableActionResult.Success(emptyList())
      }
    }

    return prisoners.entity
  }

  fun searchPrisoners(prisonerSearchRequest: PrisonerSearchRequest): List<PrisonerSearchResponse> {
    val prisoners = when (val response = prisonerSearchApiClient.prisonerSearch(prisonerSearchRequest)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data for prisonNumbers ${prisonerSearchRequest.prisonIds} Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(emptyList())
      }
    }

    return prisoners.entity
  }

  fun searchPeople(peopleSearchRequest: PeopleSearchRequest): List<PeopleSearchResponse> {
    val peoples = when (val response = prisonerSearchApiClient.peopleSearch(peopleSearchRequest)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data for prisonNumbers ${peopleSearchRequest.prisonIds} Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(emptyList())
      }
    }

    return peoples.entity
  }
}
