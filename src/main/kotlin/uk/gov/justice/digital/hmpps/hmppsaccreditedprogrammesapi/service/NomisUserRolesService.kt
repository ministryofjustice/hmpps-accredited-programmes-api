package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.NomisUserRolesApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetailResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.UserDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

@Service
@Transactional
class NomisUserRolesService(val nomisUserRolesApiClient: NomisUserRolesApiClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  private fun <T> getDetail(
    id: String,
    idType: String,
    apiCall: (String) -> ClientResult<T>,
  ): T? {
    val detail = when (val response = apiCall(id)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> {
        log.warn("Failure to get $idType details for $idType $id Reason ${response.toException().message}")
        throw ServiceUnavailableException(
          "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
          response.toException(),
        )
      }

      is ClientResult.Failure -> {
        log.warn("Failure to get $idType details for $idType $id Reason ${response.toException().message}")
        AuthorisableActionResult.Success(null)
      }
    }
    return detail.entity
  }

  fun getStaffDetail(staffId: String): StaffDetailResponse? = getDetail(staffId, "staffId") { nomisUserRolesApiClient.getStaffDetail(it) }

  fun getUserDetail(username: String): UserDetail? = getDetail(username, "username") { nomisUserRolesApiClient.getUserDetail(it) }
}
