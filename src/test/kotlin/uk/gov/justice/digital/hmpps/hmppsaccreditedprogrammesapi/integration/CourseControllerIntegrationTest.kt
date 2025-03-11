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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_OFFERING_ID
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Gender
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertTrue

val COURSE_ID: UUID = UUID.fromString("790a2dfe-8df1-4504-bb9c-83e6e53a6537")
val NEW_COURSE_ID: UUID = UUID.fromString("790a2dfe-ddd1-4504-bb9c-83e6e53a6537")
val UNUSED_COURSE_ID: UUID = UUID.fromString("891a2dfe-ddd1-4801-ab9b-94e6f53a6537")
val WITHDRAWN_COURSE_ID: UUID = UUID.fromString("44e3cdab-c996-4234-afe5-a9d8ddb13be8")
val WITHDRAWN_OFFERING_ID: UUID = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee518")

private const val OFFERING_ID = "7fffcc6a-11f8-4713-be35-cf5ff1aee517"
private const val UNUSED_OFFERING_ID = "7fffbb9a-11f8-9743-be35-cf5881aee517"

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
      COURSE_ID,
      "pr name1",
      "pr description1",
    )

    persistenceHelper.createPrerequisite(
      COURSE_ID,
      "pr name2",
      "pr description2",
    )

    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")

    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      COURSE_ID,
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createOffering(
      UUID.fromString(OFFERING_ID),
      COURSE_ID,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createOrganisation(code = "SKI", name = "SKI org")
    persistenceHelper.createEnabledOrganisation("SKI", "SKI org")

    persistenceHelper.createOffering(
      WITHDRAWN_OFFERING_ID,
      COURSE_ID,
      "SKI",
      "nobody-ski@digital.justice.gov.uk",
      "nobody2-ski@digital.justice.gov.uk",
      true,
      withdrawn = true,
    )

    persistenceHelper.createOrganisation(code = "SKN", name = "SKN org")
    persistenceHelper.createEnabledOrganisation("SKN", "SKN org")
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
      .uri("/courses/$COURSE_ID/offerings?includeWithdrawn=false")
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
      .uri("/courses/$COURSE_ID/offerings?includeWithdrawn=true")
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
    val offering = getOfferingsById(COURSE_OFFERING_ID)
    // Then
    assertThat(offering.id).isEqualTo(COURSE_OFFERING_ID)
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
    val courseId = UUID.fromString("1811faa6-d568-4fc4-83ce-41111230242f")
    val audiences = webTestClient
      .get()
      .uri("/courses/audiences?courseId=$courseId")
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
    persistenceHelper.createEnabledOrganisation("WSI", "WSI org")

    persistenceHelper.createOrganisation(code = "ESI", name = "ESI org", gender = "FEMALE")
    persistenceHelper.createEnabledOrganisation("ESI", "ESI org")

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
    val bc1MainCourseId = UUID.randomUUID()
    val bc1VariantCourseId = UUID.randomUUID()
    val bc1CourseOfferingMainId = UUID.randomUUID()
    val bc1CourseOfferingVariantId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "WSI", name = "WSI org", gender = "MALE")
    persistenceHelper.createEnabledOrganisation("WSI", "WSI org")

    persistenceHelper.createOrganisation(code = "ESI", name = "ESI org", gender = "FEMALE")
    persistenceHelper.createEnabledOrganisation("ESI", "ESI org")

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

    val buildingChoicesCourseForReferral = getBuildingChoicesCourseForReferral(referralId)

    buildingChoicesCourseForReferral.id shouldBe bc1MainCourseId
    buildingChoicesCourseForReferral.courseOfferings.size shouldBe 1
    buildingChoicesCourseForReferral.courseOfferings.first().id shouldBe bc1CourseOfferingVariantId
    buildingChoicesCourseForReferral.courseOfferings.first().organisationId shouldBe "ESI"
  }

  fun getBuildingChoicesCourseForReferral(referralId: UUID): Course = webTestClient
    .get()
    .uri("/courses/building-choices/referral/$referralId?programmePathway=HIGH_INTENSITY_BC")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody<Course>()
    .returnResult().responseBody!!

  fun getCourseVariants(
    mainCourseId: UUID,
    isConvictedOfSexualOffence: Boolean,
    isInAWomensPrison: Boolean,
  ): List<Course> = webTestClient
    .post()
    .uri("/courses/building-choices/$mainCourseId")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(
      BuildingChoicesSearchRequest(
        isConvictedOfSexualOffence = isConvictedOfSexualOffence,
        isInAWomensPrison = isInAWomensPrison,
      ),
    ).exchange()
    .expectStatus().isOk
    .expectBody<List<Course>>()
    .returnResult().responseBody!!

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
