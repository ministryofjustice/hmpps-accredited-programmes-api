package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonerNumberUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StaffService
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JwtAuthHelper::class)
class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personService: PersonService

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var staffService: StaffService

  @Autowired
  private lateinit var referralRepository: ReferralRepository

  private val offeringId1 = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  private val offeringId2 = UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537")

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      COURSE_ID,
      "SC",
      COURSE_NAME,
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")

    persistenceHelper.createOffering(
      offeringId1,
      COURSE_ID,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      offeringId2,
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createReferrerUser("ACP_TEST")
  }

  @Test
  fun `delete all ACP test referrals successful`() {
    val referralCreated1 = createReferral(offeringId1)
    val referralCreated2 = createReferral(offeringId2)

    persistenceHelper.updateReferralWithUsername(referralCreated1.id, "ACP_TEST")
    persistenceHelper.updateReferralWithUsername(referralCreated2.id, "ACP_TEST")

    deleteTestReferrals()

    persistenceHelper.getReferralById(referralCreated1.id) shouldBe 0
    persistenceHelper.getReferralById(referralCreated2.id) shouldBe 0
  }

  fun deleteTestReferrals() {
    webTestClient
      .delete()
      .uri("/admin/clean-up-test-referrals")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange()
      .expectStatus().isNoContent
  }

  fun createReferral(offeringId: UUID) = performRequestAndExpectStatusWithBody(
    httpMethod = HttpMethod.POST,
    uri = "/referrals",
    returnType = object : ParameterizedTypeReference<Referral>() {},
    body = ReferralCreate(
      offeringId = offeringId,
      prisonNumber = PRISON_NUMBER_1,
      originalReferralId = null,
    ),
    expectedResponseStatus = HttpStatus.CREATED.value(),
  )

  @Test
  fun `should return http 404 when attempting to update prisoner number of unknown prisoner`() {
    // Given
    val currentPrisonerNumber = "UNKNOWN"
    val newPrisonerNumber = "A1234DD"

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/admin/person/update-prisoner-number",
      body = PrisonerNumberUpdateRequest(
        currentPrisonerNumber = currentPrisonerNumber,
        newPrisonerNumber = newPrisonerNumber,
      ),
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should return http 404 when attempting to update prisoner number to an unknown prisoner number in NOMIS`() {
    // Given
    val currentPrisonerNumber = PRISON_NUMBER_1
    val referral = createReferral(offeringId1)
    assertThat(referral.prisonNumber).isEqualTo(PRISON_NUMBER_1)
    val newPrisonerNumber = "UNKNOWN"

    wiremockServer.stubFor(
      post(urlEqualTo("/prisoner-search/match-prisoners"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(HttpStatus.NOT_FOUND.value()),
        ),
    )

    // When & Then
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/admin/person/update-prisoner-number",
      body = PrisonerNumberUpdateRequest(
        currentPrisonerNumber = currentPrisonerNumber,
        newPrisonerNumber = newPrisonerNumber,
      ),
      expectedResponseStatus = HttpStatus.NOT_FOUND.value(),
    )
  }

  @Test
  fun `should update prisoner number on referral with new prisoner number for known person`() {
    // Given
    val newPrisonerNumber = "A1234DD"
    val referral = createReferral(offeringId1)
    assertThat(referral.prisonNumber).isEqualTo(PRISON_NUMBER_1)

    val matchedPrisoners = listOf(
      PeopleSearchResponse(
        bookingId = null,
        prisonerNumber = newPrisonerNumber,
        firstName = "John",
        lastName = "Smith",
        prisonId = "MDI",
        gender = "Male",
        ethnicity = "White",
        dateOfBirth = LocalDate.of(1980, 1, 1),
        prisonName = "Moorland (HMP & YOI)",
        conditionalReleaseDate = LocalDate.of(2023, 1, 1),
        sentenceStartDate = LocalDate.of(2022, 1, 1),
        sentenceExpiryDate = LocalDate.of(2024, 1, 1),
        religion = null,
        paroleEligibilityDate = LocalDate.of(2023, 1, 1),
        indeterminateSentence = false,
        homeDetentionCurfewEligibilityDate = null,
        tariffDate = null,
      ),
    )
    wiremockServer.stubFor(
      post(urlEqualTo("/prisoner-search/match-prisoners"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(matchedPrisoners)),
        ),
    )

    // When
    performRequestAndExpectStatusWithBody(
      httpMethod = HttpMethod.PUT,
      uri = "/admin/person/update-prisoner-number",
      body = PrisonerNumberUpdateRequest(
        currentPrisonerNumber = PRISON_NUMBER_1,
        newPrisonerNumber = newPrisonerNumber,
      ),
      expectedResponseStatus = HttpStatus.OK.value(),
    )

    // Then
    val updatedReferralList = referralRepository.findAllByPrisonNumber(newPrisonerNumber)
    assertThat(updatedReferralList).isNotEmpty
    assertThat(updatedReferralList[0].prisonNumber).isEqualTo(newPrisonerNumber)
  }
}
