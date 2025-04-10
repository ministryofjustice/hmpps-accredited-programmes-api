package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.CaseLoad
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation

private const val PRISON_API = "PRISON API"

@Component
class PrisonApiClient(
  @Qualifier("prisonApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getSentenceInformation(prisonNumber: String) = getRequest<SentenceInformation>(PRISON_API) {
    path = "/api/offenders/$prisonNumber/booking/latest/sentence-summary"
  }

  fun getCurrentUserCaseloads(allCaseloads: Boolean = false) = getRequest<List<CaseLoad>>(PRISON_API) {
    path = "/api/users/me/caseloads?allCaseloads=$allCaseloads"
  }
}
