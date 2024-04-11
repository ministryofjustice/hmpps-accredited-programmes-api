package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component
class HealthCheck() : HealthIndicator {
  override fun health(): Health = Health.up().build()
}

@Component("authHealth")
class AuthHealthPing(@Qualifier("authHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonerSearchApiHealth")
class PrisonerSearchApiHealthPing(@Qualifier("prisonerSearchApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonApiHealth")
class PrisonApiHealthPing(@Qualifier("prisonsApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonRegisterApiHealth")
class PrisonerRegisterApiHealthPing(@Qualifier("prisonRegisterApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("oasysApiHealth")
class OasysApiHealthPing(@Qualifier("oasysApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("arnsApiHealth")
class ArnsApiHealthPing(@Qualifier("arnsApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)
