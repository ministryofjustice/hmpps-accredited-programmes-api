package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores

private const val ARNS_API = "Arns API"

@Component
class ArnsApiClient(
  @Qualifier("arnsApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPredictorsAll(crn: String) = getRequest<List<ArnsScores>>(ARNS_API) {
    path = "/risks/crn/$crn/predictors/all"
  }
}
