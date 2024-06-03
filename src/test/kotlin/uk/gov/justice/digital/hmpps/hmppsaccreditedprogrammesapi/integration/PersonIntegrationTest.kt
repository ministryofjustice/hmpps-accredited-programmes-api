package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import java.time.LocalDate
import java.time.Month

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PersonIntegrationTest : IntegrationTestBase() {

  @Test
  fun `get sentences by prison number should return 200 with matching entries`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = PRISONER_1.prisonerNumber
    val sentenceDetails = getSentences(prisonNumber)
    sentenceDetails.keyDates?.forEach { println("keyDate: $it \n") }
    sentenceDetails.sentences!!.size shouldBe 5
    sentenceDetails.sentences!![0].description shouldBe "CJA03 Standard Determinate Sentence"
    sentenceDetails.keyDates!!.size.shouldBeGreaterThan(0)
    val keyDate = sentenceDetails.keyDates?.firstOrNull { it.earliestReleaseDate == true }
    keyDate!!.code.shouldBeEqual("PRRD")
  }

  @Test
  fun `get offences by offence code is successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val prisonNumber = "C6666DD"
    val offences = getOffences(prisonNumber)
    offences.isNotEmpty()

    offences.first() shouldBe Offence(
      offence = "Cause / permit fail to give treatment or cull sick or injured conventionally reared meat chickens - England - SX03174",
      category = "Contrary to regulations 5(1)(ba), 7(1)(c) and 9 of, and paragraph 11(3) of Schedule 5A to, the Welfare of Farmed Animals (England) Regulations 2007.",
      offenceDate = LocalDate.of(2012, Month.OCTOBER, 21),
    )
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

  private fun getOffences(prisonNumber: String): List<Offence> =
    webTestClient
      .get()
      .uri("/people/offences/$prisonNumber")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Offence>>()
      .returnResult().responseBody!!
}
