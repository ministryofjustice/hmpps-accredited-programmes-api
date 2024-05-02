package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonerSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PrisonerSearchIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var auditRepository: AuditRepository

  @Test
  fun `search for a prisoner by prisonId`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val prisonerSearchRequest = PrisonerSearchRequest("C6666DD", listOf("MDI"))
    val response = searchPrisoners(prisonerSearchRequest)

    response.shouldNotBeNull()
    response.first() shouldBeEqual PrisonerSearchResponse(
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

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == "C6666DD" && it.auditAction == AuditAction.NOMIS_SEARCH_FOR_PERSON.name }

    auditEntity shouldNotBe null
  }

  fun searchPrisoners(prisonerSearchRequest: PrisonerSearchRequest) =
    webTestClient
      .post()
      .uri("/prisoner-search")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(prisonerSearchRequest)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<PrisonerSearchResponse>>()
      .returnResult().responseBody!!
}
