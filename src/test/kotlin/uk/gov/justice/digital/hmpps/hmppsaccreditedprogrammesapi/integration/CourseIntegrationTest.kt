package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_OFFERING_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var courseRepository: CourseRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createCourse(
      UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      "CC",
      "Custom Course",
      "Sample description",
      "CC",
      "General offence",
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41111230242e"),
      "LEG",
      "Legacy Course",
      "Sample description",
      "LC",
      "General offence",
      true,
    )

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(
      UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"),
      UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
      "B2345BB",
      "TEST_REFERRER_USER_1",
      "This referral will be updated",
      false,
      false,
      "REFERRAL_STARTED",
      null,
    )
    persistenceHelper.createReferral(
      UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"),
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      "C3456CC",
      "TEST_REFERRER_USER_2",
      "more information",
      true,
      true,
      "REFERRAL_SUBMITTED",
      LocalDateTime.parse("2023-11-12T19:11:00"),
    )
    persistenceHelper.createReferral(
      UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"),
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      "D3456DD",
      "TEST_REFERRER_USER_2",
      "more information",
      true,
      true,
      "REFERRAL_SUBMITTED",
      LocalDateTime.parse("2023-11-13T19:11:00"),
    )

    persistenceHelper.createAudience(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e1"), name = "Intimate partner violence offence", colour = "green")
    persistenceHelper.createAudience(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e2"), name = "Sexual offence", colour = "orange")
  }

  @Test
  fun `Searching for all courses without JWT returns 401`() {
    webTestClient
      .get()
      .uri("/courses")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Searching for all course names with JWT returns 200 with correct body`() {
    val expectedCourseNames = courseRepository.getCourseNames(true)

    val responseBodySpec = webTestClient
      .get()
      .uri("/courses/course-names")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()

    responseBodySpec.jsonPath("$").isEqualTo(expectedCourseNames)
  }

  @Test
  fun `Searching for all active course names with JWT returns 200 with correct body`() {
    val expectedCourseNames = courseRepository.getCourseNames(false)

    val responseBodySpec = webTestClient
      .get()
      .uri("/courses/course-names?includeWithdrawn=false")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()

    responseBodySpec.jsonPath("$").isEqualTo(expectedCourseNames)
  }

  @Test
  fun `Searching for a course with JWT and valid id returns 200 with correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID.toString())
  }

  @Test
  fun `Searching for a course with JWT and random id returns 404 with error body`() {
    val randomId = UUID.randomUUID()

    webTestClient
      .get()
      .uri("/courses/$randomId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.status").isEqualTo(404)
      .jsonPath("$.errorCode").isEmpty
      .jsonPath("$.userMessage").value(startsWith("Not Found: No Course found at /courses/$randomId"))
      .jsonPath("$.developerMessage").value(startsWith("No Course found at /courses/$randomId"))
      .jsonPath("$.moreInfo").isEmpty
  }

  @Test
  fun `Searching for a course by offering id with JWT returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/offerings/$COURSE_OFFERING_ID/course")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID.toString())
  }

  @Test
  fun `Searching for all offerings for a course with JWT and valid id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID/offerings")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
        [
          { "organisationId": "MDI", "contactEmail":"nobody-mdi@digital.justice.gov.uk" },
          { "organisationId": "BWN", "contactEmail":"nobody-bwn@digital.justice.gov.uk" }
        ]
      """,
      )
  }

  @Test
  fun `Searching for all offerings with JWT and correct course offering id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/offerings/$COURSE_OFFERING_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_OFFERING_ID.toString())
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
  }

  @Test
  fun `Get all audiences returns 200 with correct body`() {
    webTestClient
      .get()
      .uri("/courses/audiences")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
        [
          { "name": "Intimate partner violence offence", "colour":"green" },
          { "name": "Sexual offence", "colour":"orange" }
        ]
      """,
      )
  }
}
