package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.RS256
import org.springframework.context.annotation.Bean
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
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

  fun bearerToken(): String {
    val auth = SecurityContextHolder.getContext().authentication

    val claims = mutableMapOf<String, Any>().apply {
      put("user_name", auth?.name ?: CLIENT_USERNAME)
      put("authorities", auth?.authorities?.map { it.authority } ?: listOf<String>())
      put("scope", listOf<String>())
      put("client_id", "hmpps-accredited-programmes-ui")
    }

    return Jwts.builder()
      .id(UUID.randomUUID().toString())
      .subject(auth?.name ?: CLIENT_USERNAME)
      .claims(claims)
      .expiration(Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()))
      .signWith(keyPair.private, RS256)
      .compact()
      .let { "Bearer $it" }
  }
}
