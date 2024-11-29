package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.UserDetail

private const val NOMIS_USER_ROLE_MANAGEMENT_API = "NOMIS USER ROLEMANAGEMENT API"

@Component
class NomisUserRolesApiClient(
  @Qualifier("nomisUserRolesApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getStaffDetail(staffId: String) = getRequest<StaffDetail>(NOMIS_USER_ROLE_MANAGEMENT_API) {
    path = "/users/staff/$staffId"
  }

  fun getUserDetail(username: String) = getRequest<UserDetail>(NOMIS_USER_ROLE_MANAGEMENT_API) {
    path = "/users/$username"
  }
}
