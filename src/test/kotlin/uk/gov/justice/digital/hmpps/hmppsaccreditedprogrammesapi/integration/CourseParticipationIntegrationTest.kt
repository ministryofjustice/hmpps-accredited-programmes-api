package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e"), "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("882a5a16-bcb8-4d8b-9692-a3006dcecffb"), "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("cc8eb19e-050a-4aa9-92e0-c654e5cfe281"), "C1234CC", "Orange Course", "squirrel", "This participation will be updated", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
  }

  @Test
  fun `Creating a course participation should return 201 with correct body`() {
    val startTime = LocalDateTime.now()

    val created = createCourseParticipation(
      CourseParticipationCreate(
        courseName = "Course name",
        prisonNumber = "A1234AA",
        source = "Source of information",
        detail = "Course detail",
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
          location = "location",
        ),
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.complete,
          yearStarted = 2021,
          yearCompleted = 2022,
        ),
      ),
    )

    created.shouldNotBeNull()
    created.id.shouldNotBeNull()

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = created.courseName,
        prisonNumber = created.prisonNumber,
        source = created.source,
        detail = created.detail,
        setting = CourseParticipationSetting(
          type = created.setting!!.type,
          location = created.setting?.location,
        ),
        outcome = CourseParticipationOutcome(
          status = created.outcome!!.status,
          yearStarted = created.outcome?.yearStarted,
          yearCompleted = created.outcome?.yearCompleted,
        ),
        addedBy = CLIENT_USERNAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )

    LocalDateTime.parse(retrieved.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Creating a course participation with minimal fields should tolerantly return 201 with correct body`() {
    val created = createCourseParticipation(
      CourseParticipationCreate(
        courseName = null,
        prisonNumber = randomPrisonNumber(),
        setting = null,
        outcome = null,
      ),
    )

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = null,
        prisonNumber = created.prisonNumber,
        source = null,
        detail = null,
        setting = null,
        outcome = null,
        addedBy = CLIENT_USERNAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )
  }

  @Test
  fun `Creating a course participation with invalid year value returns 400 and validation error message`() {
    val invalidCourseParticipation = CourseParticipationCreate(
      courseName = "Course name",
      prisonNumber = "A1234AA",
      source = "Source of information",
      detail = "Course detail",
      setting = CourseParticipationSetting(
        type = CourseParticipationSettingType.custody,
        location = "location",
      ),
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.complete,
        yearStarted = 1985,
        yearCompleted = 2022,
      ),
    )

    webTestClient
      .post()
      .uri("/course-participations")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(invalidCourseParticipation)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Validation failure: yearStarted is not valid.")
  }

  @Test
  fun `Updating a course participation should return 200 with correct body`() {
    val startTime = LocalDateTime.now()

    val created = createCourseParticipation(
      CourseParticipationCreate(
        prisonNumber = "A1234AA",
      ),
    )

    val updated = updateCourseParticipation(
      created.id,
      CourseParticipationUpdate(
        courseName = "Course name",
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
        ),
        source = "Source of information",
        detail = "Course participation detail",
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.incomplete,
          yearStarted = 2020,
        ),
      ),
    )!!

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = updated.courseName,
        setting = CourseParticipationSetting(
          type = updated.setting!!.type,
        ),
        prisonNumber = created.prisonNumber,
        source = updated.source,
        detail = updated.detail,
        outcome = CourseParticipationOutcome(
          status = updated.outcome!!.status,
          yearStarted = updated.outcome?.yearStarted,
          yearCompleted = updated.outcome?.yearCompleted,
        ),
        addedBy = CLIENT_USERNAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )

    updated.shouldBeEqualToIgnoringFields(retrieved, CourseParticipation::createdAt)
    LocalDateTime.parse(updated.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Attempting to update a non-existent course participation should return 404`() {
    val nonExistentId = UUID.randomUUID()

    val updateAttempt = CourseParticipationUpdate(
      courseName = "Non-existent Course",
      setting = CourseParticipationSetting(
        type = CourseParticipationSettingType.custody,
      ),
      source = "Non-existent Source",
      detail = "Non-existent Course Detail",
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.incomplete,
        yearStarted = 2021,
      ),
    )

    webTestClient
      .put()
      .uri("/course-participations/{id}", nonExistentId)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(updateAttempt)
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Finding course participations by prison number should return 200 with matching entries`() {
    val expectedPrisonNumber = randomPrisonNumber()
    val otherPrisonNumber = randomPrisonNumber()

    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", true, "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk")
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk")

    persistenceHelper.createCourse(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"), "CC", "Custom Course", "Sample description", "CC", true, "General offence")
    persistenceHelper.createCourse(UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"), "RC", "RAPID Course", "Sample description", "RC", false, "General offence")

    val expectedPrisonNumberCourseIds = getCourseIds().map {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = expectedPrisonNumber,
        ),
      ).id
    }.toSet()

    expectedPrisonNumberCourseIds.shouldNotBeEmpty()

    val randomPrisonNumber = getCourseIds().map {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = otherPrisonNumber,
        ),
      ).id
    }.toSet()

    randomPrisonNumber.intersect(expectedPrisonNumberCourseIds).shouldBeEmpty()

    val retrieved = getCourseParticipationsForPrisonNumber(expectedPrisonNumber).map { it.id }.toSet()
    retrieved shouldBe expectedPrisonNumberCourseIds
  }

  @Test
  fun `Finding course participations by random prison number should return 200 with no matching entries`() {
    getCourseIds().forEach {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = randomPrisonNumber(),
        ),
      )
    }

    getCourseParticipationsForPrisonNumber("Z0000ZZ").shouldBeEmpty()
  }

  @Test
  fun `Deleting a course participation by id should return 204 with no body`() {
    val created = createCourseParticipation(
      CourseParticipationCreate(
        prisonNumber = "X9999XX",
      ),
    )

    getCourseParticipationStatusCode(created.id) shouldBe HttpStatus.OK

    deleteCourseParticipation(created.id)
      .expectStatus().isNoContent

    getCourseParticipationStatusCode(created.id) shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `Attempting to delete a non-existent course participation should return 404`() {
    deleteCourseParticipation(UUID.randomUUID())
      .expectStatus().isNotFound
  }

  private fun createCourseParticipation(courseParticipationToAdd: CourseParticipationCreate): CourseParticipation =
    webTestClient
      .post()
      .uri("/course-participations")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(courseParticipationToAdd)
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun updateCourseParticipation(id: UUID, update: CourseParticipationUpdate): CourseParticipation? =
    webTestClient
      .put()
      .uri("/course-participations/{id}", id)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(update)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody

  private fun deleteCourseParticipation(id: UUID) =
    webTestClient
      .delete()
      .uri("/course-participations/{id}", id)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()

  private fun getCourseParticipation(id: UUID): CourseParticipation =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun getCourseParticipationStatusCode(id: UUID): HttpStatusCode =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .returnResult<Any>().status

  private fun getCourseParticipationsForPrisonNumber(prisonNumber: String): List<CourseParticipation> =
    webTestClient
      .get()
      .uri("/people/{prisonNumber}/course-participations", prisonNumber)
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!
}
