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
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${services.prisoner-search-api.base-url}") val prisonerSearchApiBaseUrl: String,
  @Value("\${services.prison-api.base-url}") val prisonsApiBaseUrl: String,
  @Value("\${services.prison-register-api.base-url}") val prisonRegisterApiBaseUrl: String,
  @Value("\${services.oasys-api.base-url}") val oasysApiBaseUrl: String,
  @Value("\${services.arns-api.base-url}") val arnsApiBaseUrl: String,
  @Value("\${hmpps.auth.url}") val hmppsAuthBaseUri: String,
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
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prison-api")

    return WebClient.builder()
      .baseUrl(prisonsApiBaseUrl)
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

  @Bean(name = ["prisonerSearchApiWebClient"])
  fun prisonerSearchApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prisoner-search-api")

    return WebClient.builder()
      .baseUrl(prisonerSearchApiBaseUrl)
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

  @Bean(name = ["prisonRegisterApiWebClient"])
  fun prisonRegisterApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("prison-register-api")

    return WebClient.builder()
      .baseUrl(prisonRegisterApiBaseUrl)
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

  @Bean(name = ["oasysApiWebClient"])
  fun oasysApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("oasys-api")

    return WebClient.builder()
      .baseUrl("$oasysApiBaseUrl/assessments")
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

  @Bean(name = ["arnsApiWebClient"])
  fun arnsApiWebClient(
    clientRegistrations: ClientRegistrationRepository,
    authorizedClients: OAuth2AuthorizedClientRepository,
    authorizedClientManager: OAuth2AuthorizedClientManager,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)

    oauth2Client.setDefaultClientRegistrationId("arns-api")

    return WebClient.builder()
      .baseUrl(arnsApiBaseUrl)
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

  @Bean(name = ["prisonerSearchApiHealthWebClient"])
  fun prisonerSearchHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(prisonerSearchApiBaseUrl)

  @Bean(name = ["prisonsApiHealthWebClient"])
  fun prisonsApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(prisonsApiBaseUrl)

  @Bean(name = ["prisonRegisterApiHealthWebClient"])
  fun prisonRegisterApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(prisonRegisterApiBaseUrl)

  @Bean(name = ["oasysApiHealthWebClient"])
  fun oasysApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(oasysApiBaseUrl)

  @Bean(name = ["arnsApiHealthWebClient"])
  fun arnsApiHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(arnsApiBaseUrl)

  @Bean(name = ["authHealthWebClient"])
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri)
}
