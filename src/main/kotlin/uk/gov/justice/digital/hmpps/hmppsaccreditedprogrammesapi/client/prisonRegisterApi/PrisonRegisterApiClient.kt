package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonDetails

@Component
class PrisonRegisterApiClient(
  @Qualifier("prisonRegisterApiWebClient")
  webClient: WebClient,
): BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPrisonDetailsByPrisonId(prisonId: UUID) = getRequest<List<PrisonDetails>> {
    path = "/prisons/id/$prisonId"
  }
}