package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi

import io.kotest.assertions.fail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PrisonerAlertsApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var prisonerAlertsApiClient: PrisonerAlertsApiClient

  @Test
  fun `should return prisoner alerts for known prison number`() {
    // Given
    val prisonNumber = "A1234AA"

    // When
    when (val response = prisonerAlertsApiClient.getPrisonerAlertsByPrisonNumber(prisonNumber)) {
      // Then
      is ClientResult.Success -> {
        assertThat(response.body).isNotNull()
        val alertsResponse = response.body
        assertThat(alertsResponse.content.size).isEqualTo(3)
        assertThat(alertsResponse.content).matches { alerts ->
          alerts.all { it.prisonNumber == "A1234AA" }
        }
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> fail("Unexpected client result: ${response::class.simpleName}")
    }
  }

  @Test
  fun `should return empty alert list for unknown prisonNumber`() {
    // Given
    val unknownPrisonNumber = "UNKNOWN"

    // When
    when (val response = prisonerAlertsApiClient.getPrisonerAlertsByPrisonNumber(unknownPrisonNumber)) {
      // Then
      is ClientResult.Success -> {
        assertThat(response.body).isNotNull()
        val alertsResponse = response.body
        assertThat(alertsResponse.content).isEmpty()
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> fail("Unexpected client result: ${response::class.simpleName}")
    }
  }
}
