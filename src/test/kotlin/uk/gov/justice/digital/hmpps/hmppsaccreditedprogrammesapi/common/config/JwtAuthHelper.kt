package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.RS256
import org.springframework.context.annotation.Bean
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
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
    val authorities = getAuthorities(auth)
    val claims = getClaims(auth, authorities)

    return buildJwt(auth?.name ?: REFERRER_USERNAME, claims)
  }

  fun bearerToken(username: String): String {
    val auth = SecurityContextHolder.getContext().authentication
    val authorities = getAuthorities(auth)

    val claims = getClaims(auth, authorities)

    return buildJwt(username, claims)
  }

  fun getClaims(
    auth: Authentication?,
    authorities: List<String>,
  ) = mutableMapOf<String, Any>().apply {
    put("user_name", auth?.name ?: REFERRER_USERNAME)
    put("authorities", authorities)
    put("scope", listOf<String>())
    put("client_id", "hmpps-accredited-programmes-ui")
  }

  private fun getAuthorities(auth: Authentication?) = auth?.authorities?.map { it.authority }?.let {
    listOf("ROLE_ACCREDITED_PROGRAMMES_API") + it
  } ?: listOf("ROLE_ACCREDITED_PROGRAMMES_API")

  private fun buildJwt(username: String, claims: MutableMap<String, Any>) = Jwts.builder()
    .id(UUID.randomUUID().toString())
    .subject(username)
    .claims(claims)
    .expiration(Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()))
    .signWith(keyPair.private, RS256)
    .compact()
    .let { "Bearer $it" }
}
