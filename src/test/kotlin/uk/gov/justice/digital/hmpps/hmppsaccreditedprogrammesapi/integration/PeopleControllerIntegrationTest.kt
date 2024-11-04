package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainOnly
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationOutcome.Status
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SentenceDetails
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PeopleControllerIntegrationTest : IntegrationTestBase() {

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

  @Test
  fun `should return course participation history with a 200 success code`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val referralId = UUID.randomUUID()
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createdOrganisation(code = "MDI", name = "MDI org")

    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferral(referralId, UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "A1234AA", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)

    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), referralId, "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e"), referralId, "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("882a5a16-bcb8-4d8b-9692-a3006dcecffb"), referralId, "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("cc8eb19e-050a-4aa9-92e0-c654e5cfe281"), referralId, "A1234AA", "Orange Course", "squirrel", "This participation will be updated", "Schulist End", "COMMUNITY", "COMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)

    // When
    val courseParticipations = getCourseParticipations("A1234AA", listOf(Status.COMPLETE, Status.INCOMPLETE))

    // Then
    courseParticipations.shouldNotBeNull()
    courseParticipations.size shouldBe 3
    courseParticipations.map { it.courseName } shouldBe listOf("Green Course", "Red Course", "Orange Course")
    courseParticipations.map { it.referralId } shouldContain referralId
  }

  @Test
  fun `should return course participation history where no associated referral exists`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    persistenceHelper.clearAllTableContent()

    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), null, "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e"), null, "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("882a5a16-bcb8-4d8b-9692-a3006dcecffb"), null, "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("cc8eb19e-050a-4aa9-92e0-c654e5cfe281"), null, "A1234AA", "Orange Course", "squirrel", "This participation will be updated", "Schulist End", "COMMUNITY", "COMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)

    // When
    val courseParticipations = getCourseParticipations("A1234AA", listOf(Status.COMPLETE, Status.INCOMPLETE))

    // Then
    courseParticipations.shouldNotBeNull()
    courseParticipations.size shouldBe 3
    courseParticipations.map { it.courseName } shouldBe listOf("Green Course", "Red Course", "Orange Course")
    courseParticipations.map { it.referralId } shouldContainOnly listOf(null)
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

  private fun getCourseParticipations(prisonNumber: String, outcomeStatus: List<Status>): List<CourseParticipation> =
    webTestClient
      .get()
      .uri { builder ->
        builder
          .path("/people/$prisonNumber/course-participation-history")
          .queryParam("outcomeStatus", outcomeStatus.joinToString(","))
          .build()
      }
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!
}
