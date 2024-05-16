package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

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
    sentenceDetails.sentences?.size shouldBe 5
    sentenceDetails.sentences?.get(0)?.description shouldBe "CJA03 Standard Determinate Sentence"
  }

  @Test
  fun `get offences by offence code is successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val offenceCode = "GA04001"

    val offences = getOffences(offenceCode)
    offences.isNotEmpty()

    offences.first() shouldBe Offence(
      id = 2162212,
      code = "GA04001",
      description = "Act as an unlicensed gangmaster",
      offenceType = "CE",
      revisionId = 614825,
      startDate = LocalDate.of(2015, Month.MARCH, 15),
      endDate = null,
      homeOfficeStatsCode = "099/98",
      homeOfficeDescription = "Acting as a gangmaster in contravention of Section 6 (prohibition of unlicensed activities)",
      changedDate = "2023-05-15T16:25:40",
      loadDate = "2024-01-17T12:00:11.520845",
      schedules = null,
      isChild = false,
      parentOffenceId = null,
      childOffenceIds = listOf(2162605),
      legislation = "Contrary to section 12(1), (3) and (4) of the Gangmasters (Licensing) Act 2004.",
      maxPeriodIsLife = false,
      maxPeriodOfIndictmentYears = null,
      custodialIndicator = "YES",
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

  private fun getOffences(offenceCode: String): List<Offence> =
    webTestClient
      .get()
      .uri("/people/offences/code/$offenceCode")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Offence>>()
      .returnResult().responseBody!!
}
