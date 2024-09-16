package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageusersApi.ManageUsersApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.manageusersApi.model.UserDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException

@Service
class ManageUsersService(
  private val manageUsersApiClient: ManageUsersApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getUserDetail(username: String): UserDetail? {
    val userDetail = when (val response = manageUsersApiClient.getUserDetail(username)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)

      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.warn("Failure to for get $username Reason ${response.toException().message} ")
        AuthorisableActionResult.Success(null)
      }
    }
    return userDetail.entity
  }
}
