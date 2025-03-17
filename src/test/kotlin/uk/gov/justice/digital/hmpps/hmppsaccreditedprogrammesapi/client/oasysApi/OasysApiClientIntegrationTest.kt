package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi

import io.kotest.assertions.fail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Level
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.LevelScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.RiskScoreLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OasysApiClientIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var oasysApiClient: OasysApiClient

  @Test
  fun `should return a pni calculation for known prison number`() {
    // Given
    val prisonNumber = "A1234AA"

    // When
    when (val response = oasysApiClient.getPniCalculation(prisonNumber)) {
      // Then
      is ClientResult.Success -> {
        assertThat(response.body).isNotNull()
        assertThat(response.status).isEqualTo(HttpStatus.OK)
        val pniResponse = response.body
        assertThat(pniResponse.pniCalculation?.pni).isEqualTo(Type.H)
        assertThat(pniResponse.pniCalculation?.sexDomain).isEqualTo(LevelScore(Level.H, 10))
        assertThat(pniResponse.pniCalculation?.riskLevel).isEqualTo(Level.H)
        assertThat(pniResponse.pniCalculation?.totalDomainScore).isEqualTo(5)
        assertThat(pniResponse.assessment?.id).isEqualTo(10082385)
        assertThat(pniResponse.assessment?.ovpRisk).isEqualTo(RiskScoreLevel.MEDIUM)
      }
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        val message = """
                   Unexpected status code result:
                   Method: ${response.method}
                   Path: ${response.path}
                   Status: ${response.status}
                   Body: ${response.body}
        """.trimIndent()
        fail(message)
      }
    }
  }

  @Test
  fun `should return NOT FOUND for unknown prison number`() {
    // Given
    val prisonNumber = "A9876BB"

    // When
    when (val response = oasysApiClient.getPniCalculation(prisonNumber)) {
      // Then
      is ClientResult.Success -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.Other<*> -> fail("Unexpected client result: ${response::class.simpleName}")
      is ClientResult.Failure.StatusCode<*> -> {
        assertThat(response.status).isEqualTo(HttpStatus.NOT_FOUND)
      }
    }
  }
}
