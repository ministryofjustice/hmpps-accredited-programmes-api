package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageOffencesApi.model.Offence

private const val MANAGE_OFFENCES_API = "Manage offences API"

@Component
class ManageOffencesApiClient(
  @Qualifier("manageOffencesApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getOffences(offenceCode: String) = getRequest<List<Offence>>(MANAGE_OFFENCES_API) {
    path = "/offences/code/$offenceCode"
  }
}
