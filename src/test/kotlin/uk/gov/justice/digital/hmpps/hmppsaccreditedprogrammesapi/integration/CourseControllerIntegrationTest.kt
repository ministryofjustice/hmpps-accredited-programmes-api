package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.BuildingChoicesSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseCreateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseIntensity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisites
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertTrue

val COURSE_ID1: UUID = UUID.randomUUID()
val COURSE_ID2: UUID = UUID.randomUUID()
val LEGACY_COURSE_ID: UUID = UUID.randomUUID()
val UNUSED_COURSE_ID: UUID = UUID.randomUUID()
val WITHDRAWN_COURSE_ID: UUID = UUID.randomUUID()
val WITHDRAWN_OFFERING_ID: UUID = UUID.randomUUID()
val OFFERING_ID1: UUID = UUID.randomUUID()
val OFFERING_ID2: UUID = UUID.randomUUID()
val OFFERING_ID3: UUID = UUID.randomUUID()
val UNUSED_OFFERING_ID: UUID = UUID.randomUUID()

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var courseRepository: CourseRepository

  @Autowired
  lateinit var offeringRepository: OfferingRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      COURSE_ID1,
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )

    persistenceHelper.createCourse(
      COURSE_ID2,
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

    persistenceHelper.createCourse(
      WITHDRAWN_COURSE_ID,
      "WITHDRAWN",
      "A withdrawn Course",
      "Withdrawn course for testing",
      "UN1",
      "General offence",
      withdrawn = true,
    )

    persistenceHelper.createPrerequisite(
      COURSE_ID1,
      "pr name1",
      "pr description1",
    )

    persistenceHelper.createPrerequisite(
      COURSE_ID1,
      "pr name2",
      "pr description2",
    )

    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createOrganisation(code = "SKI", name = "SKI org")

    persistenceHelper.createOffering(
      UUID.randomUUID(),
      COURSE_ID1,
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOffering(
      OFFERING_ID1,
      COURSE_ID1,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOffering(
      OFFERING_ID2,
      COURSE_ID2,
      "MDI",
      "nobody-ski@digital.justice.gov.uk",
      "nobody2-ski@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOffering(
      OFFERING_ID3,
      COURSE_ID2,
      "SKI",
      "nobody-ski@digital.justice.gov.uk",
      "nobody2-ski@digital.justice.gov.uk",
      true,
      withdrawn = true,
    )

    persistenceHelper.createOffering(
      WITHDRAWN_OFFERING_ID,
      COURSE_ID1,
      "SKI",
      "nobody-ski@digital.justice.gov.uk",
      "nobody2-ski@digital.justice.gov.uk",
      true,
      withdrawn = true,
    )

    persistenceHelper.createOffering(
      UNUSED_OFFERING_ID,
      COURSE_ID2,
      "BXI",
      "nobody-ski@digital.justice.gov.uk",
      "nobody2-ski@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createCourse(
      UUID.randomUUID(),
      "CC",
      "Custom Course",
      "Sample description",
      "CC",
      "General offence",
    )
    persistenceHelper.createCourse(
      UUID.randomUUID(),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
    )
    persistenceHelper.createCourse(
      UUID.randomUUID(),
      "LEG",
      "Legacy Course",
      "Sample description",
      "LC",
      "General offence",
      true,
    )

    persistenceHelper.createCourse(
      LEGACY_COURSE_ID,
      "LEGTEST",
      "Legacy Course test",
      "Sample description test",
      "LC test",
      "General offence test",
      false,
    )

    persistenceHelper.createCourse(
      UUID.randomUUID(),
      "LEGTEST 1",
      "Legacy Course test",
      "Sample description test 1",
      "LC test 1",
      "Sexual offence test",
      false,
    )

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(
      UUID.randomUUID(),
      OFFERING_ID1,
      "B2345BB",
      "TEST_REFERRER_USER_1",
      "This referral will be updated",
      false,
      false,
      "REFERRAL_STARTED",
      null,
    )
    persistenceHelper.createReferral(
      UUID.randomUUID(),
      OFFERING_ID3,
      "C3456CC",
      "TEST_REFERRER_USER_2",
      "more information",
      true,
      true,
      "REFERRAL_SUBMITTED",
      LocalDateTime.parse("2023-11-12T19:11:00"),
    )
    persistenceHelper.createReferral(
      UUID.randomUUID(),
      OFFERING_ID2,
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
    val expectedCourseNames = courseRepository.getCourseNames(false)

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
      .uri("/courses/$COURSE_ID1")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID1.toString())
  }

  @Test
  fun `Get course by Id should return withdrawn course with 200 with correct body`() {
    webTestClient
      .get()
      .uri("/courses/$WITHDRAWN_COURSE_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(WITHDRAWN_COURSE_ID.toString())
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
      .uri("/offerings/$OFFERING_ID1/course")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID1.toString())
  }

  @Test
  fun `Searching for all offerings for a course with JWT and valid id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID1/offerings?includeWithdrawn=false")
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
  fun `Searching for all offerings including withdrawn for a course with JWT and valid id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID1/offerings?includeWithdrawn=true")
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
          { "organisationId": "BWN", "contactEmail":"nobody-bwn@digital.justice.gov.uk" },
          { "organisationId": "SKI", "contactEmail":"nobody-ski@digital.justice.gov.uk" }
        ]
      """,
      )
  }

  @Test
  fun `Searching for all offerings with JWT and correct course offering id returns 200 and correct body`() {
    // Given & When
    val offering = getOfferingsById(OFFERING_ID1)
    // Then
    assertThat(offering.id).isEqualTo(OFFERING_ID1)
    assertThat(offering.organisationId).isNotEmpty()
    assertThat(offering.contactEmail).isNotEmpty()
  }

  @Test
  fun `Should return withdrawn offerings with 200 and correct body`() {
    // Given & When
    val offering = getOfferingsById(WITHDRAWN_OFFERING_ID)
    // Then
    assertThat(offering.id).isEqualTo(WITHDRAWN_OFFERING_ID)
    assertThat(offering.organisationId).isEqualTo("SKI")
    assertThat(offering.contactEmail).isEqualTo("nobody-ski@digital.justice.gov.uk")
    assertThat(offering.withdrawn).isTrue
  }

  @Test
  fun `Should return 404 with error body when searching for an offering with an unknown id`() {
    val randomId = UUID.randomUUID()

    webTestClient
      .get()
      .uri("/offerings/$randomId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.status").isEqualTo(404)
      .jsonPath("$.errorCode").isEmpty
      .jsonPath("$.userMessage").value(startsWith("Not Found: No Offering found at /offerings/$randomId"))
      .jsonPath("$.developerMessage").value(startsWith("No Offering found at /offerings/$randomId"))
      .jsonPath("$.moreInfo").isEmpty
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
  fun `Should return audiences as expected for a given courseId`() {
    val audiences = webTestClient
      .get()
      .uri("/courses/audiences?courseId=$LEGACY_COURSE_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Audience>>()
      .returnResult().responseBody!!

    audiences shouldContainOnly listOf(Audience(name = "Sexual offence test"), Audience(name = "General offence test"))
  }

  @Test
  fun `Retrieve course prerequisites returns 200 with correct body`() {
    val response = webTestClient
      .get()
      .uri("/courses/${COURSE_ID1}/prerequisites")
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
      .uri("/courses/${COURSE_ID1}/prerequisites")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CoursePrerequisites(newPrerequisites),
      )
      .exchange()
      .expectStatus().isOk
      .expectBody<CoursePrerequisites>()

    val response = webTestClient
      .get()
      .uri("/courses/${COURSE_ID1}/prerequisites")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CoursePrerequisites>()
      .returnResult().responseBody!!

    response.prerequisites!!.size shouldBeEqual 1

    response.prerequisites!![0].name shouldBeEqual "new pr name1"
    response.prerequisites!![0].description shouldBeEqual "new pr description1"
  }

  @Test
  fun `Update course is successful`() {
    // Given
    val updatedCourseName = "Legacy Course 456"

    // When
    val updatedCourse = updateCourse(COURSE_ID2, true, updatedCourseName)

    // Then
    updatedCourse.id shouldBe COURSE_ID2
    updatedCourse.name shouldBe updatedCourseName
    updatedCourse.alternateName shouldBe "SC++"
    updatedCourse.description shouldBe "Sample description"
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
      createCourse(courseName, identifier, description, audienceId, withdrawn, alternativeName, CourseIntensity.HIGH)

    createdCourse.id shouldNotBe null
    createdCourse.name shouldBe courseName
    createdCourse.identifier shouldBe identifier
    createdCourse.description shouldBe description
    createdCourse.audience shouldBe "Intimate partner violence offence"
    createdCourse.withdrawn shouldBe withdrawn
    createdCourse.alternateName shouldBe alternativeName
    createdCourse.displayName shouldBe "Legacy Course One"
    createdCourse.audienceColour shouldBe "green"
    createdCourse.intensity shouldBe CourseIntensity.HIGH.name
  }

  fun createCourse(
    courseName: String,
    identifier: String,
    description: String,
    audienceId: UUID,
    withdrawn: Boolean,
    alternativeName: String,
    intensity: CourseIntensity,
  ) = webTestClient
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
        intensity = intensity.name,
      ),
    )
    .exchange()
    .expectStatus().isCreated()
    .expectBody<Course>()
    .returnResult().responseBody!!

  fun updateCourse(courseId: UUID, withdrawn: Boolean, courseName: String) = webTestClient
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
      .uri("/courses/$COURSE_ID1/offerings/$OFFERING_ID1")
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
      .uri("/courses/$COURSE_ID1")
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
      .uri("/courses/$COURSE_ID2/offerings/$UNUSED_OFFERING_ID")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk

    offeringRepository.findById(UNUSED_OFFERING_ID) shouldBe Optional.empty()
  }

  @Test
  fun `should update a course offering and return 200`() {
    // Given
    val newCourseId = COURSE_ID2
    val courseOffering = CourseOffering(
      id = UNUSED_OFFERING_ID,
      organisationId = "BXI",
      contactEmail = "awi1@whatton.com",
      referable = true,
      secondaryContactEmail = "awi2@whatton.com",
      withdrawn = false,
      gender = Gender.MALE,
    )

    // When
    val updatedCourseOffering = updateCourseOffering(newCourseId, courseOffering)

    // Then
    assertThat(updatedCourseOffering.id).isEqualTo(courseOffering.id)
    assertThat(updatedCourseOffering.organisationId).isEqualTo("BXI")
    assertThat(updatedCourseOffering.contactEmail).isEqualTo("awi1@whatton.com")
    assertThat(updatedCourseOffering.secondaryContactEmail).isEqualTo("awi2@whatton.com")
    assertThat(updatedCourseOffering.referable).isTrue
    assertThat(updatedCourseOffering.withdrawn).isFalse
    assertThat(updatedCourseOffering.gender).isEqualTo(Gender.MALE)
  }

  private fun updateCourseOffering(
    courseId: UUID,
    courseOffering: CourseOffering,
  ): CourseOffering = performRequestAndExpectStatusWithBody(
    HttpMethod.PUT,
    uri = "/courses/$courseId/offerings",
    body = courseOffering,
    returnType = courseOfferingTypeReference(),
    expectedResponseStatus = HttpStatus.OK.value(),
  )

  fun courseOfferingTypeReference(): ParameterizedTypeReference<CourseOffering> = object : ParameterizedTypeReference<CourseOffering>() {}

  @Test
  fun `Create offerings returns 200`() {
    webTestClient
      .post()
      .uri("/courses/$COURSE_ID2/offerings")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseOffering(
          id = UUID.randomUUID(),
          organisationId = "AWI",
          contactEmail = "awi1@whatton.com",
          secondaryContactEmail = "awi2@whatton.com",
          referable = true,
          withdrawn = false,
          gender = Gender.MALE,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseOffering>()
      .returnResult().responseBody!!
  }

  @Test
  fun `should create offering when offering id is not provided`() {
    webTestClient
      .post()
      .uri("/courses/$COURSE_ID2/offerings")
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
          gender = Gender.MALE,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseOffering>()
      .returnResult().responseBody!!
  }

  @Test
  fun `Building choices courses are returned as expected`() {
    val bc1MainCourseId = UUID.randomUUID()
    val bc1VariantCourseId = UUID.randomUUID()
    val bc1CourseOfferingMainId = UUID.randomUUID()
    val bc1CourseOfferingVariantId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "WSI", name = "WSI org", gender = "MALE")
    persistenceHelper.createOrganisation(code = "ESI", name = "ESI org", gender = "FEMALE")

    persistenceHelper.createCourse(
      bc1MainCourseId,
      "BCH-1",
      "Building Choices: high intensity",
      "Building Choices helps people to develop high...",
      "BCH-1",
      "Sexual offence",
    )

    persistenceHelper.createCourse(
      bc1VariantCourseId,
      "BCH-2",
      "Building Choices: medium intensity",
      "Building Choices helps people to develop medium...",
      "BCH-2",
      "General offence",
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingVariantId,
      bc1MainCourseId,
      "ESI",
      "nobody-wsi@digital.justice.gov.uk",
      "nobody2-wsi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingMainId,
      bc1VariantCourseId,
      "WSI",
      "nobody-esi@digital.justice.gov.uk",
      "nobody2-esi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createCourseVariant(courseId = bc1MainCourseId, variantCourseId = bc1VariantCourseId)

    val courseVariants = getCourseVariants(bc1MainCourseId, isConvictedOfSexualOffence = true, isInAWomensPrison = true)

    courseVariants.size shouldBe 1

    val buildingChoicesCourse = courseVariants[0]
    buildingChoicesCourse.id shouldBe bc1MainCourseId
    buildingChoicesCourse.name shouldBe "Building Choices: high intensity"
    buildingChoicesCourse.displayOnProgrammeDirectory shouldBe true
    buildingChoicesCourse.courseOfferings[0].id shouldBe bc1CourseOfferingVariantId
    buildingChoicesCourse.courseOfferings[0].organisationId shouldBe "ESI"
  }

  @Test
  fun `Building choices course fetched for matching intensity and audience`() {
    // Given
    val bc1MainCourseId = UUID.randomUUID()
    val bc1VariantCourseId = UUID.randomUUID()
    val bc1CourseOfferingMainId = UUID.randomUUID()
    val bc1CourseOfferingVariantId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "WSI", name = "WSI org", gender = "MALE")
    persistenceHelper.createOrganisation(code = "ESI", name = "ESI org", gender = "FEMALE")

    persistenceHelper.createCourse(
      bc1MainCourseId,
      "BCH-1",
      "Building Choices: high intensity",
      "Building Choices helps people to develop high...",
      "BCH-1",
      "Sexual offence",
    )

    persistenceHelper.createCourse(
      bc1VariantCourseId,
      "BCH-2",
      "Building Choices: medium intensity",
      "Building Choices helps people to develop medium...",
      "BCH-2",
      "General offence",
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingVariantId,
      bc1MainCourseId,
      "ESI",
      "nobody-wsi@digital.justice.gov.uk",
      "nobody2-wsi@digital.justice.gov.uk",
      referable = true,
      withdrawn = false,
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingMainId,
      bc1VariantCourseId,
      "WSI",
      "nobody-esi@digital.justice.gov.uk",
      "nobody2-esi@digital.justice.gov.uk",
      referable = true,
      withdrawn = false,
    )

    val referralId = UUID.randomUUID()
    persistenceHelper.createReferral(
      referralId,
      bc1CourseOfferingVariantId,
      "B2345BB",
      "TEST_REFERRER_USER_1",
      "This referral will be updated",
      false,
      false,
      "REFERRAL_SUBMITTED",
      null,
    )

    persistenceHelper.createCourseVariant(courseId = bc1MainCourseId, variantCourseId = bc1VariantCourseId)

    // When
    val buildingChoicesCourseForReferral = getBuildingChoicesCourseForReferral(referralId)

    // Then
    buildingChoicesCourseForReferral.id shouldBe bc1MainCourseId
    buildingChoicesCourseForReferral.courseOfferings.size shouldBe 1
    buildingChoicesCourseForReferral.courseOfferings.first().id shouldBe bc1CourseOfferingVariantId
    buildingChoicesCourseForReferral.courseOfferings.first().organisationId shouldBe "ESI"
  }

  @Test
  fun `should return 404 not found when no matching Building choices courses are found for intensity and audience`() {
    // Given
    val bc1MainCourseId = UUID.randomUUID()
    val bc1VariantCourseId = UUID.randomUUID()
    val bc1CourseOfferingMainId = UUID.randomUUID()
    val bc1CourseOfferingVariantId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "WSI", name = "WSI org", gender = "MALE")
    persistenceHelper.createOrganisation(code = "ESI", name = "ESI org", gender = "FEMALE")

    persistenceHelper.createCourse(
      bc1MainCourseId,
      "BCH-1",
      "Building Choices: high intensity",
      "Building Choices helps people to develop high...",
      "BCH-1",
      "Sexual offence",
    )

    persistenceHelper.createCourse(
      bc1VariantCourseId,
      "BCH-2",
      "Building Choices: medium intensity",
      "Building Choices helps people to develop medium...",
      "BCH-2",
      "General offence",
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingVariantId,
      bc1MainCourseId,
      "ESI",
      "nobody-wsi@digital.justice.gov.uk",
      "nobody2-wsi@digital.justice.gov.uk",
      referable = false,
      withdrawn = false,
    )

    persistenceHelper.createOffering(
      bc1CourseOfferingMainId,
      bc1VariantCourseId,
      "WSI",
      "nobody-esi@digital.justice.gov.uk",
      "nobody2-esi@digital.justice.gov.uk",
      referable = true,
      withdrawn = false,
    )

    val referralId = UUID.randomUUID()
    persistenceHelper.createReferral(
      referralId,
      bc1CourseOfferingVariantId,
      "B2345BB",
      "TEST_REFERRER_USER_1",
      "This referral will be updated",
      false,
      false,
      "REFERRAL_SUBMITTED",
      null,
    )

    persistenceHelper.createCourseVariant(courseId = bc1MainCourseId, variantCourseId = bc1VariantCourseId)

    // When
    val errorResponse = performRequestAndExpectStatus(
      HttpMethod.GET,
      "/courses/building-choices/referral/$referralId?programmePathway=HIGH_INTENSITY_BC",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    // Then
    errorResponse.userMessage shouldBe "Not Found: Building choices course Building Choices: high intensity not offered at ESI org for audience Sexual offence"
    errorResponse.status shouldBe 404
  }

  fun getBuildingChoicesCourseForReferral(referralId: UUID): Course = performRequestAndExpectOk(
    HttpMethod.GET,
    "/courses/building-choices/referral/$referralId?programmePathway=HIGH_INTENSITY_BC",
    object : ParameterizedTypeReference<Course>() {},
  )

  fun getCourseVariants(
    mainCourseId: UUID,
    isConvictedOfSexualOffence: Boolean,
    isInAWomensPrison: Boolean,
  ): List<Course> = performRequestAndExpectStatusWithBody(
    HttpMethod.POST,
    "/courses/building-choices/$mainCourseId",
    object : ParameterizedTypeReference<List<Course>>() {},
    BuildingChoicesSearchRequest(isConvictedOfSexualOffence = isConvictedOfSexualOffence, isInAWomensPrison = isInAWomensPrison),
    HttpStatus.OK.value(),
  )

  @Test
  fun `should return courses matching intensity`() {
    val courseIdWithHighIntensity = UUID.randomUUID()
    persistenceHelper.createCourse(
      courseId = courseIdWithHighIntensity,
      identifier = "KAZ-1",
      name = "Kaizen",
      description = "kaizen general violence offence...",
      altName = "KAZ-1",
      audience = "General violence offence",
      intensity = CourseIntensity.HIGH.name,
    )

    val courseIdWithHighAndModerateIntensity = UUID.randomUUID()
    persistenceHelper.createCourse(
      courseId = courseIdWithHighAndModerateIntensity,
      identifier = "KAZ-2",
      name = "Kaizen",
      description = "kaizen general violence offence...",
      altName = "KAZ-2",
      audience = "General violence offence",
      intensity = CourseIntensity.HIGH.name + "," + CourseIntensity.MODERATE.name,
    )

    val courses = webTestClient
      .get()
      .uri("/courses?includeWithdrawn=false&intensity=HIGH")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody<List<Course>>()
      .returnResult().responseBody!!

    assertTrue {
      courses.map { it.id }.containsAll(listOf(courseIdWithHighIntensity, courseIdWithHighAndModerateIntensity))
    }
  }

  @Test
  fun `Only return building choices courses `() {
    val bc1MainCourseId = UUID.randomUUID()
    val bc1VariantCourseId = UUID.randomUUID()

    persistenceHelper.createCourse(
      bc1MainCourseId,
      "BCH-1",
      "Building Choices: high intensity",
      "Building Choices helps people to develop high...",
      "BCH-1",
      "Sexual offence",
    )

    persistenceHelper.createCourse(
      bc1VariantCourseId,
      "BCH-2",
      "Building Choices: medium intensity",
      "Building Choices helps people to develop medium...",
      "BCH-2",
      "General offence",
    )

    persistenceHelper.createCourseVariant(courseId = bc1MainCourseId, variantCourseId = bc1VariantCourseId)

    val courses = webTestClient
      .get()
      .uri("/courses?buildingChoicesOnly=true")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody<List<Course>>()
      .returnResult().responseBody!!

    courses.size shouldBe 2

    courses.map { it.id }.containsAll(listOf(bc1MainCourseId, bc1VariantCourseId))
  }

  fun getOfferingsById(id: UUID): CourseOffering = webTestClient
    .get()
    .uri("/offerings/$id")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody<CourseOffering>()
    .returnResult().responseBody!!
}
