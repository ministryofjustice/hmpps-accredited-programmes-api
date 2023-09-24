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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.TEST_USER_NAME
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationHistoryIntegrationTest
@Autowired
constructor(
  val webTestClient: WebTestClient,
  val jwtAuthHelper: JwtAuthHelper,
) {
  @Test
  fun `Add and retrieve a course participation history - happy flow`() {
    val startTime = LocalDateTime.now()
    val courseId = getFirstCourseId()

    val cpa = webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          prisonNumber = "A1234AA",
          setting = CourseParticipationSetting(
            type = CourseParticipationSettingType.custody,
            location = "location",
          ),
          outcome = CourseParticipationOutcome(
            status = CourseParticipationOutcome.Status.complete,
            yearStarted = 2021,
            yearCompleted = 2022,
            detail = "Some detail",
          ),
        ),
      ).exchange()
      .expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

    cpa.shouldNotBeNull()
    cpa.id.shouldNotBeNull()

    val courseParticipation = webTestClient
      .get()
      .uri("/course-participations/{id}", cpa.id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

    courseParticipation.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = cpa.id,
        courseId = courseId,
        prisonNumber = "A1234AA",
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
          location = "location",
        ),
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.complete,
          yearStarted = 2021,
          yearCompleted = 2022,
          detail = "Some detail",
        ),
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )
    LocalDateTime.parse(courseParticipation.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Add a course participation history with courseId and otherCourseName is rejected`() {
    val courseId = getFirstCourseId()

    val errorResponse = webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          otherCourseName = "A Course",
          prisonNumber = "A1234AA",
          setting = CourseParticipationSetting(type = CourseParticipationSettingType.custody),
          outcome = CourseParticipationOutcome(),
        ),
      ).exchange()
      .expectStatus().isBadRequest
      .expectBody(ErrorResponse::class.java)
      .returnResult().responseBody!!

    errorResponse shouldBe ErrorResponse(
      HttpStatus.BAD_REQUEST,
      developerMessage = "Expected just one of courseId or otherCourseName but both values are present",
      userMessage = "Business rule violation: Expected just one of courseId or otherCourseName but both values are present",
    )
  }

  @Test
  fun `Update a course participation history`() {
    val startTime = LocalDateTime.now()
    val courseId = getFirstCourseId()

    val courseParticipationId = addCourseParticipationHistory(courseId, "A1234AA")

    val updatedCourseParticipation = webTestClient
      .put()
      .uri("/course-participations/{id}", courseParticipationId)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseParticipationUpdate(
          courseId = courseId,
          setting = CourseParticipationSetting(
            type = CourseParticipationSettingType.custody,
          ),
          outcome = CourseParticipationOutcome(
            status = CourseParticipationOutcome.Status.incomplete,
            detail = "Some detail",
            yearStarted = 2020,
          ),
        ),
      ).exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody

    val expectedCourseParticipation = CourseParticipation(
      id = courseParticipationId,
      courseId = courseId,
      setting = CourseParticipationSetting(
        type = CourseParticipationSettingType.custody,
      ),
      prisonNumber = "A1234AA",
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.incomplete,
        detail = "Some detail",
        yearStarted = 2020,
      ),
      addedBy = TEST_USER_NAME,
      createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
    )

    updatedCourseParticipation.shouldBeEqualToIgnoringFields(expectedCourseParticipation, CourseParticipation::createdAt)
    LocalDateTime.parse(updatedCourseParticipation!!.createdAt) shouldBeGreaterThanOrEqualTo startTime
    getCourseParticipation(courseParticipationId).shouldBeEqualToIgnoringFields(expectedCourseParticipation, CourseParticipation::createdAt)
  }

  @Test
  fun `find course participation history by prison number returns matches`() {
    val ids = getCourseIds().map { addCourseParticipationHistory(it, "A1234AA") }.toSet()
    ids.shouldNotBeEmpty()

    val otherIds = getCourseIds().map { addCourseParticipationHistory(it, "Z9999ZZ") }.toSet()

    otherIds.intersect(ids).shouldBeEmpty()

    val courseIdsForPrisonNumber = webTestClient
      .get()
      .uri("/people/{prisonNumber}/course-participations", "A1234AA")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!.map { it.id }.toSet()

    courseIdsForPrisonNumber shouldBe ids
  }

  @Test
  fun `find course participation history by prison number returns no matches`() {
    getCourseIds().map { addCourseParticipationHistory(it, "A1234AA") }

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
  fun `delete course participation history`() {
    val id = addCourseParticipationHistory(getFirstCourseId(), "X9999XX")
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

  private fun addCourseParticipationHistory(courseId: UUID, prisonNumber: String): UUID =
    webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          prisonNumber = prisonNumber,
          setting = CourseParticipationSetting(type = CourseParticipationSettingType.community),
          outcome = CourseParticipationOutcome(),
        ),
      ).exchange()
      .expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!.id

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
