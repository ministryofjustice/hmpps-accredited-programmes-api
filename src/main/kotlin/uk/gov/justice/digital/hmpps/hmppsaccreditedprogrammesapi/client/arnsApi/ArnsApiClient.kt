package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsSummary

@Component
class ArnsApiClient(
  @Qualifier("arnsApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getSummary(crn: String) = getRequest<ArnsSummary> {
    path = "/risks/crn/$crn/summary"
  }

  fun getPredictorsAll(crn: String) = getRequest<List<ArnsScores>> {
    path = "/risks/crn/$crn/predictors/all"
  }
}
