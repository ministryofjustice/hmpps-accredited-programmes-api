package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.NomisAlert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation

@Component
class PrisonApiClient(
  @Qualifier("prisonApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getAlertsByPrisonNumber(prisonNumber: String) = getRequest<List<NomisAlert>> {
    path = "/api/offenders/$prisonNumber/alerts/v2"
  }

  fun getSentenceInformation(prisonNumber: String) = getRequest<SentenceInformation> {
    path = "/api/offenders/$prisonNumber/booking/latest/sentence-summary"
  }
}
