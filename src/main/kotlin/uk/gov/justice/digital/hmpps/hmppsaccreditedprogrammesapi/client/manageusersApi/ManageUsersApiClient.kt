package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageusersApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageusersApi.model.UserDetail

private const val MANAGE_USERS_API = "Manage Users API"

@Component
class ManageUsersApiClient(
  @Qualifier("manageUsersApiWebClient")
  private val webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getUserDetail(username: String) = getRequest<UserDetail>(MANAGE_USERS_API) {
    path = "/users/$username"
  }
}
