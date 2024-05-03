package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonsByIdsRequest

private const val PRISON_REGISTER_API = "PrisonRegister API"

@Component
class PrisonRegisterApiClient(
  @Qualifier("prisonRegisterApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPrison(prisonId: String) = getRequest<Prison>(PRISON_REGISTER_API) {
    path = "/prisons/id/$prisonId"
  }

  fun getPrisons(prisonIds: List<String>) = postRequest<List<Prison>>(PRISON_REGISTER_API) {
    path = "/prisons/prisonsByIds"
    body = PrisonsByIdsRequest(prisonIds)
  }
}
