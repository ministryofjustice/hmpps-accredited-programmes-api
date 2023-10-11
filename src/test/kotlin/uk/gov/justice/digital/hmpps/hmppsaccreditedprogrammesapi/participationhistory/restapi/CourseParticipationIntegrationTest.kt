package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.TEST_USER_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationIntegrationTest
@Autowired
constructor(
  val webTestClient: WebTestClient,
  val jwtAuthHelper: JwtAuthHelper,
) {
  @Test
  fun `Creating a course participation should return 201 with correct body`() {
    val startTime = LocalDateTime.now()
    val courseId = getFirstCourseId()

    val cpa = createCourseParticipation(
      CreateCourseParticipation(
        courseId = courseId,
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

    cpa.shouldNotBeNull()
    cpa.id.shouldNotBeNull()

    val courseParticipation = getCourseParticipation(cpa.id)

    courseParticipation.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = cpa.id,
        courseId = courseId,
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
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )

    LocalDateTime.parse(courseParticipation.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Creating a course participation with minimal fields should tolerantly return 201 with correct body`() {
    val courseId = getFirstCourseId()
    val prisonNumber = randomPrisonNumber()

    val cpa = createCourseParticipation(
      CreateCourseParticipation(
        courseId = courseId,
        prisonNumber = prisonNumber,
        setting = null,
        outcome = null,
      ),
    )

    val courseParticipation = getCourseParticipation(cpa.id)

    courseParticipation.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = cpa.id,
        courseId = courseId,
        prisonNumber = prisonNumber,
        source = null,
        detail = null,
        setting = null,
        outcome = null,
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )
  }

  @Test
  fun `Creating a course participation with courseId and otherCourseName should return 400 with error body`() {
    val courseId = getFirstCourseId()

    val courseParticipation = CreateCourseParticipation(
      courseId = courseId,
      otherCourseName = "A Course",
      prisonNumber = "A1234AA",
      source = "Source of information",
      detail = "Course detail",
      setting = CourseParticipationSetting(type = CourseParticipationSettingType.custody),
      outcome = null,
    )

    val errorResponse = webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(courseParticipation)
      .exchange()
      .expectStatus().isBadRequest
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!

    errorResponse shouldBe ErrorResponse(
      HttpStatus.BAD_REQUEST,
      developerMessage = "Expected just one of courseId or otherCourseName but both values are present",
      userMessage = "Business rule violation: Expected just one of courseId or otherCourseName but both values are present",
    )
  }

  @Test
  fun `Updating a course participation should return 200 with correct body`() {
    val startTime = LocalDateTime.now()
    val courseId = getFirstCourseId()
    val courseParticipationId = createCourseParticipation(minimalCourseParticipation(courseId, "A1234AA")).id
    val updatedSource = "Source of information"
    val updatedDetail = "Updated course participation detail"

    val courseParticipationFromUpdate = updateCourseParticipation(
      courseParticipationId,
      CourseParticipationUpdate(
        courseId = courseId,
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
        ),
        source = updatedSource,
        detail = updatedDetail,
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.incomplete,
          yearStarted = 2020,
        ),
      ),
    )!!

    val expectedCourseParticipation = CourseParticipation(
      id = courseParticipationId,
      courseId = courseId,
      setting = CourseParticipationSetting(
        type = CourseParticipationSettingType.custody,
      ),
      prisonNumber = "A1234AA",
      source = updatedSource,
      detail = updatedDetail,
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.incomplete,
        yearStarted = 2020,
      ),
      addedBy = TEST_USER_NAME,
      createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
    )

    courseParticipationFromUpdate.shouldBeEqualToIgnoringFields(expectedCourseParticipation, CourseParticipation::createdAt)
    LocalDateTime.parse(courseParticipationFromUpdate.createdAt) shouldBeGreaterThanOrEqualTo startTime
    getCourseParticipation(courseParticipationId).shouldBeEqualToIgnoringFields(expectedCourseParticipation, CourseParticipation::createdAt)
  }

  @Test
  fun `Finding course participations by prison number should return 200 with matching entries`() {
    val prisonNumber = randomPrisonNumber()
    val ids = getCourseIds().map { createCourseParticipation(minimalCourseParticipation(it, prisonNumber)).id }.toSet()
    ids.shouldNotBeEmpty()

    val otherIds = getCourseIds().map { createCourseParticipation(minimalCourseParticipation(it, randomPrisonNumber())) }.toSet()

    otherIds.intersect(ids).shouldBeEmpty()

    val courseIdsForPrisonNumber = webTestClient
      .get()
      .uri("/people/{prisonNumber}/course-participations", prisonNumber)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!.map { it.id }.toSet()

    courseIdsForPrisonNumber shouldBe ids
  }

  @Test
  fun `Finding course participations by random prison number should return 200 with no matching entries`() {
    val prisonNumber = randomPrisonNumber()
    getCourseIds().map { createCourseParticipation(minimalCourseParticipation(it, prisonNumber)) }

    webTestClient
      .get()
      .uri("/people/{prisonNumber}/course-participations", "Z0000ZZ")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!.shouldBeEmpty()
  }

  @Test
  fun `Deleting a course participation by id should return 204 with no body`() {
    val id = createCourseParticipation(minimalCourseParticipation(getFirstCourseId(), "X9999XX")).id
    getCourseParticipationStatusCode(id) shouldBe HttpStatus.OK

    webTestClient
      .delete()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent

    getCourseParticipationStatusCode(id) shouldBe HttpStatus.NOT_FOUND

    webTestClient
      .delete()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  private fun getFirstCourseId(): UUID = getCourseIds().first()

  private fun getCourseIds(): List<UUID> =
    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody<List<Course>>()
      .returnResult().responseBody!!.map { it.id }

  private fun createCourseParticipation(courseParticipationToAdd: CreateCourseParticipation) =
    webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(courseParticipationToAdd)
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun updateCourseParticipation(courseParticipationId: UUID, update: CourseParticipationUpdate): CourseParticipation? =
    webTestClient
      .put()
      .uri("/course-participations/{id}", courseParticipationId)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(update)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody

  private fun getCourseParticipation(id: UUID): CourseParticipation =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun getCourseParticipationStatusCode(id: UUID): HttpStatusCode =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .returnResult<Any>().status
}

private fun minimalCourseParticipation(courseId: UUID, prisonNumber: String) = CreateCourseParticipation(
  courseId = courseId,
  prisonNumber = prisonNumber,
)
