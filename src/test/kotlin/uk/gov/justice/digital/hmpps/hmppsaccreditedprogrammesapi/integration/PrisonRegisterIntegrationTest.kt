package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Organisation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PrisonRegisterIntegrationTest : IntegrationTestBase() {

  @Test
  fun `get prison id and prison names from prison register api`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val prisons = getPrisons()
    prisons.isNotEmpty()

    prisons[0].id shouldBe "AWI"
    prisons[0].name shouldBe "Ashwell (HMP)"

    prisons[1].id shouldBe "BXI"
    prisons[1].name shouldBe "Brixton (HMP)"
  }

  private fun getPrisons() =
    webTestClient
      .get()
      .uri("/organisations")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Organisation>>()
      .returnResult().responseBody!!
}
