package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PersonIntegrationTest : IntegrationTestBase() {

  @Test
  fun `get sentences by prison number should return 200 with matching entries`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = PRISONER_1.prisonerNumber

    val sentenceDetails = getSentences(prisonNumber)
    sentenceDetails.sentences?.size shouldBe 5
    sentenceDetails.sentences?.get(0)?.description shouldBe "CJA03 Standard Determinate Sentence"
  }

  private fun getSentences(prisonNumber: String): SentenceDetails =
    webTestClient
      .get()
      .uri("/people/$prisonNumber/sentences")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<SentenceDetails>()
      .returnResult().responseBody!!
}
