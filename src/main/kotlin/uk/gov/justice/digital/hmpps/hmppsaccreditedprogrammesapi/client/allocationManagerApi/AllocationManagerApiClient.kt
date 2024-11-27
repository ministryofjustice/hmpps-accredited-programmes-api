package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.OffenderAllocation

private const val ALLOCATION_MANAGER_API = "ALLOCATION MANAGER API"

@Component
class AllocationManagerApiClient(
  @Qualifier("allocationManagerApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getPomDetails(prisonNumber: String) = getRequest<OffenderAllocation>(ALLOCATION_MANAGER_API) {
    path = "/api/allocation/$prisonNumber"
  }
}
