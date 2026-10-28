package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerNumbers
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonerSearchRequest

private const val PRISONER_SEARCH_API = "PrisonerSearch API"

@Component
class PrisonerSearchApiClient(
  @Qualifier("prisonerSearchApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPrisonersByPrisonNumbers(prisonNumbers: List<String>) = postRequest<List<Prisoner>>(PRISONER_SEARCH_API) {
    path = "/prisoner-search/prisoner-numbers"
    body = PrisonerNumbers(prisonNumbers)
  }

  /**
   * Exact lookup by prison number returning the full people-search projection. Uses the same
   * prisoner-search document as [peopleSearch] so no fields are lost, but is a reliable exact match
   * (unlike the match-prisoners search, which can return no results for a valid prison number).
   */
  fun getPeopleByPrisonNumbers(prisonNumbers: List<String>) = postRequest<List<PeopleSearchResponse>>(PRISONER_SEARCH_API) {
    path = "/prisoner-search/prisoner-numbers"
    body = PrisonerNumbers(prisonNumbers)
  }
  fun prisonerSearch(prisonerSearchRequest: PrisonerSearchRequest) = postRequest<List<PrisonerSearchResponse>>(PRISONER_SEARCH_API) {
    path = "/prisoner-search/match-prisoners"
    body = prisonerSearchRequest
  }

  fun peopleSearch(peopleSearchRequest: PeopleSearchRequest) = postRequest<List<PeopleSearchResponse>>(PRISONER_SEARCH_API) {
    path = "/prisoner-search/match-prisoners"
    body = peopleSearchRequest
  }
}
