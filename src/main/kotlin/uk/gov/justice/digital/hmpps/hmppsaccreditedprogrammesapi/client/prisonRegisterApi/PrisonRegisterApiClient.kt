package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache.WebClientCache
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonDetails

@Component
class PrisonRegisterApiClient(
  @Qualifier("prisonRegisterApiWebClient")
  webClient: WebClient,
  webClientCache: WebClientCache,
) : BaseHMPPSClient(webClient, jacksonObjectMapper(), webClientCache) {

  fun getPrisonDetailsByPrisonId(prisonId: String) = getRequest<List<PrisonDetails>> {
    path = "/prisons/id/$prisonId"
  }

  fun getAllPrisonDetails() = getRequest<List<PrisonDetails>> {
    path = "/prisons/names"
  }
}
