package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config.ErrorResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OasysApiIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get offence details from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonId = "A9999BB"
    val offenceDetail = getOffenceDetailsByPrisonId(prisonId)

    offenceDetail.shouldNotBeNull()
    offenceDetail shouldBeEqual OffenceDetail(
      offenceDetails = "An attack took place on christmas eve in Alfreds ex partners house. The children were in bed and the dog was left out side.",
      contactTargeting = false,
      raciallyMotivated = false,
      revenge = true,
      domesticViolence = true,
      repeatVictimisation = true,
      victimWasStranger = true,
      stalking = true,
      recognisesImpact = false,
      numberOfOthersInvolved = null,
      othersInvolvedDetail = null,
      peerGroupInfluences = "No",
      motivationAndTriggers = "Mainly due to jealousy and fuelled by drug use",
      acceptsResponsibility = false,
      acceptsResponsibilityDetail = "This has happened numerous times in the past",
      patternOffending = null,
    )
  }

  @Test
  fun `Get offence details from Oasys with invalid prison number`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonId = "Z9999ZZ"
    val errorResponse = getOffenceDetailsByPrisonId404(prisonId)
    errorResponse shouldBeEqual
      ErrorResponse(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Not Found: No assessment found for prisoner id: Z9999ZZ",
        developerMessage = "No assessment found for prisoner id: Z9999ZZ",
      )
  }

  fun getOffenceDetailsByPrisonId(prisonId: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonId/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<OffenceDetail>()
      .returnResult().responseBody!!

  fun getOffenceDetailsByPrisonId404(prisonId: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonId/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is4xxClientError
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!
}
