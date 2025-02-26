package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Address
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Category
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonOperator
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PrisonSearchIntegrationTest : IntegrationTestBase() {

  @Test
  fun `search for prisons by prisonIds`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val prisonerSearchRequest = PrisonSearchRequest(listOf("MDI"))
    val response = searchPrisons(prisonerSearchRequest)

    response.shouldNotBeNull()
    response.first() shouldBeEqual PrisonSearchResponse(
      prisonId = "MDI",
      prisonName = "Moorland HMP",
      active = true,
      male = true,
      female = true,
      contracted = true,
      types = listOf(PrisonType(code = "HMP", description = "His Majesty’s Prison")),
      categories = listOf(Category(category = "A")),
      addresses = listOf(
        Address(
          addressLine1 = "Bawtry Road",
          town = "Doncaster",
          postcode = "DN7 6BW",
          country = "England",
          addressLine2 = "Hatfield Woodhouse",
          county = "South Yorkshire",
        ),
      ),
      operators = listOf(PrisonOperator(name = "PSP, G4S")),
    )
  }

  @Test
  fun `search for a prison by prisonId`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getPrison("MDI")

    response.shouldNotBeNull()
    response shouldBeEqual PrisonSearchResponse(
      prisonId = "MDI",
      prisonName = "Moorland (HMP & YOI)",
      active = true,
      male = true,
      female = false,
      contracted = false,
      types = listOf(PrisonType(code = "HMP", description = "His Majesty’s Prison")),
      categories = listOf(Category(category = "A")),
      addresses = listOf(
        Address(
          addressLine1 = "Bawtry Road",
          town = "Doncaster",
          postcode = "DN7 6BW",
          country = "England",
          addressLine2 = "Hatfield Woodhouse",
          county = "South Yorkshire",
        ),
      ),
      operators = listOf(PrisonOperator(name = "PSP")),
    )
  }

  fun searchPrisons(prisonSearchRequest: PrisonSearchRequest) = webTestClient
    .post()
    .uri("/prison-search")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(prisonSearchRequest)
    .exchange()
    .expectStatus().isOk
    .expectBody<List<PrisonSearchResponse>>()
    .returnResult().responseBody!!

  private fun getPrison(prisonNumber: String): PrisonSearchResponse = webTestClient
    .get()
    .uri("/prison-search/$prisonNumber")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<PrisonSearchResponse>()
    .returnResult().responseBody!!
}
