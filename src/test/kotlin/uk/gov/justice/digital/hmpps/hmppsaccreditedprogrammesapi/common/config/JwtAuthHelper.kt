package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration

/**
 * Test helper for building bearer tokens for use in integration/controller tests.
 *
 * Token generation is delegated to the framework-provided [JwtAuthorisationHelper] so that the
 * resulting JWT is signed with the same key pair as the auto-configured `jwtDecoder` bean and is
 * therefore accepted by the application's resource server. We continue to populate the
 * `ROLE_ACCREDITED_PROGRAMMES_API` authority and the `client_id` claim that the application expects.
 */
@Component
class JwtAuthHelper(
  private val jwtAuthorisationHelper: JwtAuthorisationHelper,
) {

  fun bearerToken(): String {
    val auth = SecurityContextHolder.getContext().authentication
    return buildJwt(auth?.name ?: REFERRER_USERNAME, getAuthorities(auth))
  }

  fun bearerToken(username: String): String {
    val auth = SecurityContextHolder.getContext().authentication
    return buildJwt(username, getAuthorities(auth))
  }

  private fun getAuthorities(auth: Authentication?) = auth?.authorities?.map { it.authority }?.let {
    listOf("ROLE_ACCREDITED_PROGRAMMES_API") + it
  } ?: listOf("ROLE_ACCREDITED_PROGRAMMES_API")

  private fun buildJwt(username: String, authorities: List<String>): String {
    val token = jwtAuthorisationHelper.createJwtAccessToken(
      username = username,
      clientId = "hmpps-accredited-programmes-ui",
      roles = authorities,
      scope = listOf(),
      expiryTime = Duration.ofHours(1),
    )
    return "Bearer $token"
  }
}
