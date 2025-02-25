package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import java.util.Base64

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
  private val objectMapper: ObjectMapper,
) {

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
    .csrf { it.disable() }
    .authorizeHttpRequests {
      it
        .requestMatchers(
          "/health/**",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/api.yml",
          "/info",
          "/swagger-ui.html",
        ).permitAll()
        .requestMatchers("/subject-access-request").hasAnyRole("SAR_DATA_ACCESS", "ACCREDITED_PROGRAMMES_API")
        .anyRequest().hasRole("ACCREDITED_PROGRAMMES_API")
    }
    .anonymous { it.disable() }
    .oauth2ResourceServer { resourceServer ->
      resourceServer.jwt { jwt -> jwt.jwtAuthenticationConverter(AuthAwareTokenConverter()) }
        .authenticationEntryPoint { _, response, _ ->
          response.apply {
            status = 401
            contentType = "application/problem+json"
            characterEncoding = "UTF-8"
            writer.write(
              objectMapper.writeValueAsString(
                mapOf(
                  "title" to "Unauthenticated",
                  "status" to 401,
                  "detail" to "A valid HMPPS Auth JWT must be supplied via bearer authentication to access this endpoint",
                ),
              ),
            )
          }
        }
    }
    .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
    .build()
}

class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): AbstractAuthenticationToken = AuthAwareAuthenticationToken(
    jwt,
    findPrincipal(jwt.claims),
    extractAuthorities(jwt),
  )

  private fun findPrincipal(claims: Map<String, Any?>): String = when {
    claims.containsKey(CLAIM_USERNAME) -> claims[CLAIM_USERNAME]
    claims.containsKey(CLAIM_USER_ID) -> claims[CLAIM_USER_ID]
    claims.containsKey(CLAIM_CLIENT_ID) -> claims[CLAIM_CLIENT_ID]
    else -> throw RuntimeException("Unable to find a claim to identify Subject by")
  } as String

  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
    val grantedAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt) ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    val claimStrings = (jwt.claims[CLAIM_AUTHORITY] as Collection<String>?) ?: emptyList()

    return grantedAuthorities + claimStrings.map(::SimpleGrantedAuthority)
  }

  companion object {
    const val CLAIM_USERNAME = "user_name"
    const val CLAIM_USER_ID = "user_id"
    const val CLAIM_CLIENT_ID = "client_id"
    const val CLAIM_AUTHORITY = "authorities"
  }
}

@Configuration
class AuthorizedClientServiceConfiguration(
  @Value("\${log-client-credentials-jwt-info}") private val logClientCredentialsJwtInfo: Boolean,
  private val clientRegistrationRepository: ClientRegistrationRepository,
  private val objectMapper: ObjectMapper,
) {

  @Bean
  fun inMemoryOAuth2AuthorizedClientService(): OAuth2AuthorizedClientService = if (logClientCredentialsJwtInfo) {
    LoggingInMemoryOAuth2AuthorizedClientService(clientRegistrationRepository, objectMapper)
  } else {
    InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
  }
}

class LoggingInMemoryOAuth2AuthorizedClientService(
  clientRegistrationRepository: ClientRegistrationRepository,
  private val objectMapper: ObjectMapper,
) : OAuth2AuthorizedClientService {

  private val backingImplementation = InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
  private val log = LoggerFactory.getLogger(this::class.java)

  override fun <T : OAuth2AuthorizedClient?> loadAuthorizedClient(
    clientRegistrationId: String?,
    principalName: String?,
  ): T = backingImplementation.loadAuthorizedClient(clientRegistrationId, principalName)

  override fun saveAuthorizedClient(authorizedClient: OAuth2AuthorizedClient?, principal: Authentication?) {
    authorizedClient?.accessToken?.tokenValue?.let { tokenValue ->
      try {
        val tokenBodyBase64 = tokenValue.split(".")[1]
        val tokenBodyRaw = Base64.getDecoder().decode(tokenBodyBase64)
        val info = objectMapper.readValue(tokenBodyRaw, JwtLogInfo::class.java)
        log.info(
          "Retrieved a client_credentials JWT for service->service calls for client ${authorizedClient.clientRegistration.clientId} with authorities: ${info.authorities}, scopes: ${info.scope}, expiry: ${info.exp}",
        )
      } catch (exception: Exception) {
        log.error("Unable to get token info to log, exception of type: ${exception::class.java.name}")
      }
    }

    backingImplementation.saveAuthorizedClient(authorizedClient, principal)
  }

  data class JwtLogInfo(
    val authorities: List<String>,
    val scope: List<String>,
    val exp: Long,
  )

  override fun removeAuthorizedClient(clientRegistrationId: String?, principalName: String?) = backingImplementation.removeAuthorizedClient(clientRegistrationId, principalName)
}

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val aPrincipal: String,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {

  override fun getPrincipal(): String = aPrincipal
}
