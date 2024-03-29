package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonerSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerNumbers
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PrisonerSearchResponse

@Component
class PrisonerSearchApiClient(
  @Qualifier("prisonerSearchApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPrisonersByPrisonNumbers(prisonNumbers: List<String>) = postRequest<List<Prisoner>> {
    path = "/prisoner-search/prisoner-numbers"
    body = PrisonerNumbers(prisonNumbers)
  }
  fun prisonerSearch(prisonerSearchRequest: PrisonerSearchRequest) = postRequest<List<PrisonerSearchResponse>> {
    path = "/prisoner-search/match-prisoners"
    body = prisonerSearchRequest
  }
}
