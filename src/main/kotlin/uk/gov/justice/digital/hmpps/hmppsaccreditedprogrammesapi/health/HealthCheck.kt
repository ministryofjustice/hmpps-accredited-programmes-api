package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component
class PrisonerSearchApiHealthCheck(@Qualifier("prisonerSearchApiWebClient") webClient: WebClient) :
  HealthPingCheck(webClient)

@Component
class PrisonApiHealthCheck(@Qualifier("prisonApiWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component
class PrisonRegisterApiHealthCheck(@Qualifier("prisonRegisterApiWebClient") webClient: WebClient) :
  HealthPingCheck(webClient)

@Component
class OasysApiWebClientHealthCheck(@Qualifier("oasysApiWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component
class ManageOffencesApiWebClientHealthCheck(@Qualifier("manageOffencesApiWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component
class CaseNotesApiWebClientHealthCheck(@Qualifier("caseNotesApiWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component
class ManageUsersApiWebClientHealthCheck(@Qualifier("manageUsersApiWebClient") webClient: WebClient) : HealthPingCheck(webClient)
