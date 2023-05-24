package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
  private val objectMapper: ObjectMapper,
) {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? = http
    .csrf { it.disable() }
    .authorizeHttpRequests {
      it
        .requestMatchers(
          "/health/**",
          "/swagger-ui/**",
          "/v3/api-docs/swagger-config",
          "/api.yml",
          "/info",
        ).permitAll()
        .anyRequest().authenticated()
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
                object {
                  val title = "Unauthenticated"
                  val status = 401
                  val detail =
                    "A valid HMPPS Auth JWT must be supplied via bearer authentication to access this endpoint"
                },
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

  override fun convert(jwt: Jwt): AbstractAuthenticationToken =
    AuthAwareAuthenticationToken(
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

class AuthAwareAuthenticationToken(
  jwt: Jwt,
  private val aPrincipal: String,
  authorities: Collection<GrantedAuthority>,
) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): String {
    return aPrincipal
  }
}
