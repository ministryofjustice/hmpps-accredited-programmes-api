package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${upstream-timeout-ms}") private val upstreamTimeoutMs: Long,
  @Value("\${max-response-in-memory-size-bytes}") private val maxResponseInMemorySizeBytes: Int,
) {

  @Bean
  fun authorizedClientManager(clients: ClientRegistrationRepository): OAuth2AuthorizedClientManager {
    val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clients)
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service)
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()
    manager.setAuthorizedClientProvider(authorizedClientProvider)
    return manager
  }

  @Bean(name = ["prisonApiWebClient"])
  fun prisonsApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.prison-api.base-url}") prisonsApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prison-api")
    return buildWebClient(prisonsApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["prisonerSearchApiWebClient"])
  fun prisonerSearchApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.prisoner-search-api.base-url}") prisonerSearchApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prisoner-search-api")
    return buildWebClient(prisonerSearchApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["prisonRegisterApiWebClient"])
  fun prisonRegisterApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.prison-register-api.base-url}") prisonRegisterApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prison-register-api")
    return buildWebClient(prisonRegisterApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["oasysApiWebClient"])
  fun oasysApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.oasys-api.base-url}") oasysApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("oasys-api")
    return buildWebClient("$oasysApiBaseUrl/assessments", oauth2Client)
  }

  @Bean(name = ["arnsApiWebClient"])
  fun arnsApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.arns-api.base-url}") arnsApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("arns-api")

    return buildWebClient(arnsApiBaseUrl, oauth2Client)
  }

  @Bean(name = ["manageOffencesApiWebClient"])
  fun manageOffencesApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    @Value("\${services.manage-offences-api.base-url}") manageOffencesApiBaseUrl: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("arns-api")

    return buildWebClient(manageOffencesApiBaseUrl, oauth2Client)
  }

  fun buildWebClient(url: String, oauth2Client: ServletOAuth2AuthorizedClientExchangeFilterFunction): WebClient = WebClient.builder()
    .baseUrl(url)
    .clientConnector(
      ReactorClientHttpConnector(
        HttpClient
          .create()
          .responseTimeout(Duration.ofMillis(upstreamTimeoutMs))
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Duration.ofMillis(upstreamTimeoutMs).toMillis().toInt()),
      ),
    )
    .exchangeStrategies(
      ExchangeStrategies.builder().codecs {
        it.defaultCodecs().maxInMemorySize(maxResponseInMemorySizeBytes)
      }.build(),
    )
    .filter(oauth2Client)
    .build()
}
