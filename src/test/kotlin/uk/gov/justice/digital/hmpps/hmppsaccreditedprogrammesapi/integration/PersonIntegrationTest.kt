package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SentenceDetails
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
  fun `get sentences by prison number with no keyDates should return 200 empty keydates`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val sentenceDetails = getSentences("A8610DY")
    sentenceDetails.keyDates?.size shouldBe 0
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

  @Test
  fun `search for a person by prisonId`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val peopleSearchRequest = PeopleSearchRequest("C6666DD", listOf("MDI"))
    val response = searchPrisoners(peopleSearchRequest)

    response.shouldNotBeNull()
    response.first() shouldBeEqual PeopleSearchResponse(
      bookingId = "1202335",
      conditionalReleaseDate = null,
      prisonName = "Nottingham (HMP)",
      dateOfBirth = LocalDate.of(1975, 1, 1),
      ethnicity = "White: Eng./Welsh/Scot./N.Irish/British",
      gender = "Male",
      homeDetentionCurfewEligibilityDate = null,
      indeterminateSentence = false,
      firstName = "MICKEY",
      lastName = "SMITH",
      paroleEligibilityDate = null,
      prisonerNumber = "C6666DD",
      religion = null,
      sentenceExpiryDate = null,
      sentenceStartDate = null,
      tariffDate = null,
    )
  }

  fun searchPrisoners(peopleSearchRequest: PeopleSearchRequest) =
    webTestClient
      .post()
      .uri("/prisoner-search")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(peopleSearchRequest)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<PeopleSearchResponse>>()
      .returnResult().responseBody!!

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
