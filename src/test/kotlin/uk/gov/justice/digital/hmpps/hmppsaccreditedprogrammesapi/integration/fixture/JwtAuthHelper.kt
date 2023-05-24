package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
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
    subject = "hmpps-accredited-programmes-ui",
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
    claims["client_id"] = "court-reg-client"

    return Jwts.builder()
      .setId(jwtId)
      .setSubject(subject)
      .addClaims(claims)
      .setExpiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
      .signWith(keyPair.private, SignatureAlgorithm.RS256)
      .compact()
  }
}
