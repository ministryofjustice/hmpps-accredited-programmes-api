package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model.AlertsResponse

@Component
class PrisonerAlertsApiClient(
  @Qualifier("prisonerAlertsApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPrisonerAlertsByPrisonNumber(prisonNumber: String) = getRequest<AlertsResponse>("Prisoner Alerts API") {
    path = "/prisoners/$prisonNumber/alerts"
  }
}
