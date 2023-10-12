package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.RS256
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.TEST_USER_NAME
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

@Component
class JwtAuthHelper {
  private val keyPair: KeyPair = with(KeyPairGenerator.getInstance("RSA")) {
    initialize(2048)
    generateKeyPair()
  }

  /**
   * The jwtDecoder bean is injected into the Spring OAuth2 configuration.
   */
  @Bean
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun authorizationHeaderConfigurer() = { headers: HttpHeaders -> headers.set(HttpHeaders.AUTHORIZATION, bearerToken()) }

  fun bearerToken(): String = createJwt(
    subject = TEST_USER_NAME,
    expiryTime = Duration.ofHours(1L),
  ).let { "Bearer $it" }

  private fun createJwt(
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
  ): String {
    val claims = mutableMapOf<String, Any>()

    subject?.let { claims["user_name"] = it }
    roles?.let { claims["authorities"] = it }
    scope?.let { claims["scope"] = it }
    claims["client_id"] = "hmpps-accredited-programmes-ui"

    return Jwts.builder()
      .id(jwtId)
      .subject(subject)
      .claims(claims)
      .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
      .signWith(keyPair.private, RS256)
      .compact()
  }
}
