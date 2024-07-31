package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseCreateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisites
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_OFFERING_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

val COURSE_ID: UUID = UUID.fromString("790a2dfe-8df1-4504-bb9c-83e6e53a6537")
val NEW_COURSE_ID: UUID = UUID.fromString("790a2dfe-ddd1-4504-bb9c-83e6e53a6537")
val UNUSED_COURSE_ID: UUID = UUID.fromString("891a2dfe-ddd1-4801-ab9b-94e6f53a6537")

private const val OFFERING_ID = "7fffcc6a-11f8-4713-be35-cf5ff1aee517"
private const val UNUSED_OFFERING_ID = "7fffbb9a-11f8-9743-be35-cf5881aee517"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var courseRepository: CourseRepository

  @Autowired
  lateinit var offeringRepository: OfferingRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      COURSE_ID,
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )

    persistenceHelper.createCourse(
      NEW_COURSE_ID,
      "NEWSC",
      "A new Course",
      "Sample description",
      "SC++",
      "General offence",
    )

    persistenceHelper.createCourse(
      UNUSED_COURSE_ID,
      "UNUSEDC",
      "An unused Course",
      "Unused course for testing",
      "UN1",
      "General offence",
    )

    persistenceHelper.createPrerequisite(
      COURSE_ID,
      "pr name1",
      "pr description1",
    )

    persistenceHelper.createPrerequisite(
      COURSE_ID,
      "pr name2",
      "pr description2",
    )
    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      COURSE_ID,
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      UUID.fromString(OFFERING_ID),
      COURSE_ID,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOffering(
      UUID.fromString(UNUSED_OFFERING_ID),
      NEW_COURSE_ID,
      "SKN",
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

    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41111230242f"),
      "LEGTEST",
      "Legacy Course test",
      "Sample description test",
      "LC test",
      "General offence test",
      false,
    )

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(
      UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"),
      UUID.fromString(OFFERING_ID),
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

    persistenceHelper.createAudience(
      UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e1"),
      name = "Intimate partner violence offence",
      colour = "green",
    )
    persistenceHelper.createAudience(
      UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e2"),
      name = "Sexual offence",
      colour = "orange",
    )
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
          { "id":"28e47d30-30bf-4dab-a8eb-9fda3f6400e1", "name": "Intimate partner violence offence", "colour":"green" },
          { "id":"28e47d30-30bf-4dab-a8eb-9fda3f6400e2", "name": "Sexual offence", "colour":"orange" }
        ]
      """,
      )
  }

  @Test
  fun `Retrieve course prerequisites returns 200 with correct body`() {
    val response = webTestClient
      .get()
      .uri("/courses/${COURSE_ID}/prerequisites")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CoursePrerequisites>()
      .returnResult().responseBody!!

    response.prerequisites!!.size shouldBeGreaterThan 0

    val sortedResponse = response.prerequisites!!.sortedBy { it.name }

    sortedResponse[0].name shouldBeEqual "pr name1"
    sortedResponse[1].name shouldBeEqual "pr name2"
    sortedResponse[0].description shouldBeEqual "pr description1"
    sortedResponse[1].description shouldBeEqual "pr description2"
  }

  @Test
  fun `Update course prerequisites returns 200 with correct body`() {
    val newPrerequisites = listOf(CoursePrerequisite("new pr name1", "new pr description1"))

    webTestClient
      .put()
      .uri("/courses/${COURSE_ID}/prerequisites")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CoursePrerequisites(newPrerequisites),
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<CoursePrerequisites>()

    val getResponse = webTestClient
      .get()
      .uri("/courses/${COURSE_ID}/prerequisites")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CoursePrerequisites>()
      .returnResult().responseBody!!

    getResponse.prerequisites!!.size shouldBeEqual 1

    getResponse.prerequisites!![0].name shouldBeEqual "new pr name1"
    getResponse.prerequisites!![0].description shouldBeEqual "new pr description1"
  }

  @Test
  fun `Update course is successful`() {
    val updatedCourseName = "Legacy Course 456"
    val courseIdToUpdate = "1811faa6-d568-4fc4-83ce-41111230242f"
    val updatedCourse = updateCourse(UUID.fromString(courseIdToUpdate), true, updatedCourseName)

    updatedCourse.id shouldBe UUID.fromString(courseIdToUpdate)
    updatedCourse.name shouldBe updatedCourseName
    updatedCourse.alternateName shouldBe "LC test"
    updatedCourse.description shouldBe "Sample description test"
    updatedCourse.withdrawn shouldBe true
  }

  @Test
  fun `Create course is successful`() {
    val courseName = "Legacy Course One"
    val identifier = "LC1"
    val description = "Test description for Legacy Course"
    val audienceId = UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e1")
    val withdrawn = false
    val alternativeName = "LCO"

    val createdCourse =
      createCourse(courseName, identifier, description, audienceId, withdrawn, alternativeName)

    createdCourse.id shouldNotBe null
    createdCourse.name shouldBe courseName
    createdCourse.identifier shouldBe identifier
    createdCourse.description shouldBe description
    createdCourse.audience shouldBe "Intimate partner violence offence"
    createdCourse.withdrawn shouldBe withdrawn
    createdCourse.alternateName shouldBe alternativeName
    createdCourse.displayName shouldBe "Legacy Course One"
    createdCourse.audienceColour shouldBe "green"
  }

  fun createCourse(
    courseName: String,
    identifier: String,
    description: String,
    audienceId: UUID,
    withdrawn: Boolean,
    alternativeName: String,
  ) =
    webTestClient
      .post()
      .uri("/courses")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseCreateRequest(
          name = courseName,
          identifier = identifier,
          description = description,
          audienceId = audienceId,
          withdrawn = withdrawn,
          alternateName = alternativeName,
        ),
      )
      .exchange()
      .expectStatus().isCreated()
      .expectBody<Course>()
      .returnResult().responseBody!!

  fun updateCourse(courseId: UUID, withdrawn: Boolean, courseName: String) =
    webTestClient
      .put()
      .uri("/courses/$courseId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseUpdateRequest(
          name = courseName,
          withdrawn = withdrawn,
        ),
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<Course>()
      .returnResult().responseBody!!

  @Test
  fun `Delete a course offering that is in use returns 400`() {
    webTestClient
      .delete()
      .uri("/courses/$COURSE_ID/offerings/$OFFERING_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is4xxClientError
      .expectBody()
      .jsonPath("$.userMessage")
      .isEqualTo("Business rule violation: Offering is in use and cannot be deleted. This offering should be withdrawn")
  }

  @Test
  fun `Delete a course that is in use returns 400`() {
    webTestClient
      .delete()
      .uri("/courses/$COURSE_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is4xxClientError
      .expectBody()
      .jsonPath("$.userMessage")
      .isEqualTo("Business rule violation: Cannot delete course as offerings exist that use this course.")
  }

  @Test
  fun `Delete a course that is not in use returns a 200`() {
    webTestClient
      .delete()
      .uri("/courses/$UNUSED_COURSE_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
    courseRepository.findById(UNUSED_COURSE_ID) shouldBe Optional.empty()
  }

  @Test
  fun `Delete a course offering that is not in use returns 200`() {
    webTestClient
      .delete()
      .uri("/courses/$NEW_COURSE_ID/offerings/$UNUSED_OFFERING_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk

    offeringRepository.findById(UUID.fromString(UNUSED_OFFERING_ID)) shouldBe Optional.empty()
  }

  @Test
  fun `Create offerings returns 200`() {
    webTestClient
      .post()
      .uri("/courses/$NEW_COURSE_ID/offerings")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseOffering(
          id = null,
          organisationId = "AWI",
          contactEmail = "awi1@whatton.com",
          secondaryContactEmail = "awi2@whatton.com",
          referable = true,
          withdrawn = false,
          organisationEnabled = true,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseOffering>()
      .returnResult().responseBody!!
  }
}
