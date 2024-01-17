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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config.ErrorResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OasysApiIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get offence details from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val offenceDetail = getOffenceDetailsByPrisonNumber(prisonNumber)

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
    val prisonNumber = "Z9999ZZ"
    val errorResponse = getOffenceDetailsByPrisonNumber404(prisonNumber)
    errorResponse shouldBeEqual
      ErrorResponse(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Not Found: No assessment found for prison number: Z9999ZZ",
        developerMessage = "No assessment found for prison number: Z9999ZZ",
      )
  }

  @Test
  fun `Get relationships from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val relationships = getRelationshipsByPrisonNumber(prisonNumber)

    relationships.shouldNotBeNull()
    relationships shouldBeEqual Relationships(
      dvEvidence = true,
      victimFormerPartner = true,
      victimFamilyMember = true,
      victimOfPartnerFamily = true,
      perpOfPartnerOrFamily = true,
      relIssuesDetails = "Free text",
    )
  }

  @Test
  fun `Get rosh analysis from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val roshAnalysis = getRoshAnalysisByPrisonNumber(prisonNumber)

    roshAnalysis.shouldNotBeNull()
    roshAnalysis shouldBeEqual RoshAnalysis(
      offenceDetails = "Assault with a base ball bat",
      whereAndWhen = "in the park",
      howDone = "with a base ball bat",
      whoVictims = "the gardener",
      anyoneElsePresent = "noone",
      whyDone = "anger issues",
      source = "local police",
    )
  }

  fun getRoshAnalysisByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/rosh-analysis")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshAnalysis>()
      .returnResult().responseBody!!

  fun getRelationshipsByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/relationships")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Relationships>()
      .returnResult().responseBody!!

  fun getOffenceDetailsByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<OffenceDetail>()
      .returnResult().responseBody!!

  fun getOffenceDetailsByPrisonNumber404(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is4xxClientError
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!
}
