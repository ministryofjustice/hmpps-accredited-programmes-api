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
    val matchResults = matchPrisoners(peopleSearchRequest)
    if (matchResults.isNotEmpty()) {
      log.debug("People search (match-prisoners) returned {} match(es) for {}", matchResults.size, peopleSearchRequest.describeCriteria())
      return matchResults
    }

    // match-prisoners returned no matches. When a full prison number was supplied, fall back to the
    // exact prisoner-numbers lookup, which reliably finds people that the match-prisoners search can
    // miss - the cause of "cannot be found" in Find a Programme. See APG-2764.
    val prisonNumber = peopleSearchRequest.prisonerIdentifier?.trim()
    if (prisonNumber.isNullOrBlank()) {
      log.warn("People search returned no matches for ${peopleSearchRequest.describeCriteria()}")
      return emptyList()
    }

    val byPrisonNumber = peopleByPrisonNumbers(listOf(prisonNumber))
      .filter { peopleSearchRequest.prisonIds.isNullOrEmpty() || it.prisonId in peopleSearchRequest.prisonIds!! }

    if (byPrisonNumber.isEmpty()) {
      log.warn("People search returned no matches for ${peopleSearchRequest.describeCriteria()} (match-prisoners and prisoner-numbers)")
    } else {
      log.info("match-prisoners returned no matches but exact prisoner-numbers lookup found {} for prisonerIdentifier=$prisonNumber. See APG-2764.", byPrisonNumber.size)
    }
    return byPrisonNumber
  }

  private fun matchPrisoners(peopleSearchRequest: PeopleSearchRequest): List<PeopleSearchResponse> = when (val response = prisonerSearchApiClient.peopleSearch(peopleSearchRequest)) {
    is ClientResult.Success -> response.body

    is ClientResult.Failure.Other -> throw ServiceUnavailableException(
      "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
      response.toException(),
    )

    is ClientResult.Failure -> {
      log.warn("Failure to retrieve data for ${peopleSearchRequest.describeCriteria()} Reason ${response.toException().message} ")
      emptyList()
    }
  }

  private fun peopleByPrisonNumbers(prisonNumbers: List<String>): List<PeopleSearchResponse> = when (val response = prisonerSearchApiClient.getPeopleByPrisonNumbers(prisonNumbers)) {
    is ClientResult.Success -> response.body

    is ClientResult.Failure.Other -> throw ServiceUnavailableException(
      "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
      response.toException(),
    )

    is ClientResult.Failure -> {
      log.warn("Failure to retrieve data for prisonNumbers $prisonNumbers Reason ${response.toException().message} ")
      emptyList()
    }
  }

  // Names are intentionally omitted (PII); boolean flags preserve enough signal to tell a NOMIS-number
  // search apart from a name-based search without logging the names themselves.
  private fun PeopleSearchRequest.describeCriteria(): String = "prisonerIdentifier=$prisonerIdentifier, prisonIds=$prisonIds, " +
    "firstNameProvided=${!firstName.isNullOrBlank()}, lastNameProvided=${!lastName.isNullOrBlank()}"
}
