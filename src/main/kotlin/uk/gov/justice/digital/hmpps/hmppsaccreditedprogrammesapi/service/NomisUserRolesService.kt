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

  fun getStaffDetail(staffId: String): StaffDetailResponse? {
    val staffDetail = when (val response = nomisUserRolesApiClient.getStaffDetail(staffId)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> {
        log.warn("Failure to for get staff details for staffId $staffId Reason ${response.toException().message} ")
        throw ServiceUnavailableException(
          "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
          response.toException(),
        )
      }

      is ClientResult.Failure -> {
        log.warn("Failure to for get staff details for staffId $staffId Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(null)
      }
    }
    return staffDetail.entity
  }

  fun getUserDetail(username: String): UserDetail? {
    val userDetail = when (val response = nomisUserRolesApiClient.getUserDetail(username)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> {
        log.warn("Failure to for get user details for username $username Reason ${response.toException().message} ")
        throw ServiceUnavailableException(
          "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
          response.toException(),
        )
      }

      is ClientResult.Failure -> {
        log.warn("Failure to for get staff details for username $username Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(null)
      }
    }
    return userDetail.entity
  }
}
