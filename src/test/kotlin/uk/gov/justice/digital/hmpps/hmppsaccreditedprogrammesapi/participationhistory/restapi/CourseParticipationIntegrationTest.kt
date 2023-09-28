package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Creating a course participation should return 201 with correct body`() {
    val courseId = getFirstCourseId()
    val prisonNumber = randomPrisonNumber()

    val createdCourseParticipationId = createCourseParticipation(
      courseId = courseId,
      prisonNumber = prisonNumber,
    ).expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!.id

    createdCourseParticipationId shouldNotBe null

    val retrievedCourseParticipation = getCourseParticipation(createdCourseParticipationId)

    val expectedCourseParticipation = CourseParticipation(
      id = createdCourseParticipationId,
      courseId = courseId,
      prisonNumber = prisonNumber,
    )

    retrievedCourseParticipation shouldBe expectedCourseParticipation
  }

  @Test
  fun `Creating a course participation with a valid course id but a different course name should return 400 with error body`() {
    val createCourseParticipationErrorResponse = createCourseParticipation(
      courseId = getFirstCourseId(),
      otherCourseName = "A Course",
      prisonNumber = randomPrisonNumber(),
    ).expectStatus().isBadRequest
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!

    createCourseParticipationErrorResponse shouldBe ErrorResponse(
      HttpStatus.BAD_REQUEST,
      developerMessage = "Expected just one of courseId or otherCourseName but both values are present",
      userMessage = "Business rule violation: Expected just one of courseId or otherCourseName but both values are present",
    )
  }

  @Test
  fun `Updating a course participation with a valid payload should return 200 with correct body`() {
    val courseId = getFirstCourseId()
    val prisonNumber = randomPrisonNumber()

    val courseParticipationId = createCourseParticipation(
      courseId = courseId,
      prisonNumber = prisonNumber,
    ).expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!.id

    val updatedCourseParticipation = webTestClient
      .put()
      .uri("/course-participations/$courseParticipationId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        CourseParticipationUpdate(
          courseId = courseId,
          yearStarted = 2020,
          setting = CourseSetting.custody,
          outcome = CourseParticipationOutcome(
            status = CourseParticipationOutcome.Status.deselected,
            detail = "Some detail",
          ),
        ),
      )
      .exchange().expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

    val expectedCourseParticipation = CourseParticipation(
      id = courseParticipationId,
      courseId = courseId,
      yearStarted = 2020,
      setting = CourseSetting.custody,
      prisonNumber = prisonNumber,
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.deselected,
        detail = "Some detail",
      ),
    )

    updatedCourseParticipation shouldBe expectedCourseParticipation
    getCourseParticipation(courseParticipationId) shouldBe expectedCourseParticipation
  }

  @Test
  fun `Searching for a course participation with a valid prison number should return 200 with correct body`() {
    val prisonNumber = randomPrisonNumber()
    val otherPrisonNumber = randomPrisonNumber()

    val createdCourseParticipationIds = getCourseIds().map {
      createCourseParticipation(
        courseId = it,
        prisonNumber = prisonNumber,
      ).expectStatus().isCreated
        .expectBody<CourseParticipation>()
        .returnResult().responseBody!!.id
    }.toSet()

    createdCourseParticipationIds.shouldNotBeEmpty()

    val unrelatedCourseParticipationIds = getCourseIds().map {
      createCourseParticipation(
        courseId = it,
        prisonNumber = otherPrisonNumber,
      ).expectStatus().isCreated
        .expectBody<CourseParticipation>()
        .returnResult().responseBody!!.id
    }.toSet()

    unrelatedCourseParticipationIds.intersect(createdCourseParticipationIds).shouldBeEmpty()

    val courseIdsForPrisonNumber = webTestClient
      .get()
      .uri("/people/$prisonNumber/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!
      .map { it.id }.toSet()

    courseIdsForPrisonNumber shouldBe createdCourseParticipationIds
  }

  @Test
  fun `Searching for a course participation with a random prison number should return 200 with empty body`() {
    getCourseIds().forEach {
      createCourseParticipation(
        courseId = it,
        prisonNumber = randomPrisonNumber(),
      ).expectStatus().isCreated
        .expectBody<CourseParticipation>()
        .returnResult().responseBody!!
    }

    val randomPrisonNumber = randomPrisonNumber()

    val courseParticipationsForPrisonNumber = webTestClient
      .get()
      .uri("/people/$randomPrisonNumber/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!

    courseParticipationsForPrisonNumber shouldBe emptyList()
  }

  @Test
  fun `Deleting a course participation returns 204 with no body`() {
    val randomPrisonNumber = randomPrisonNumber()

    val createdCourseParticipationId = createCourseParticipation(
      courseId = getFirstCourseId(),
      prisonNumber = randomPrisonNumber,
    ).expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!.id

    val retrievedCourseParticipationId = getCourseParticipation(createdCourseParticipationId).id

    webTestClient
      .delete()
      .uri("/course-participations/$retrievedCourseParticipationId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent

    webTestClient
      .get()
      .uri("/course-participations/$createdCourseParticipationId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound

    webTestClient
      .delete()
      .uri("/course-participations/$retrievedCourseParticipationId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  fun getCourseParticipation(id: UUID): CourseParticipation =
    webTestClient
      .get()
      .uri("/course-participations/$id")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  fun createCourseParticipation(
    courseId: UUID,
    otherCourseName: String? = null,
    prisonNumber: String,
  ): WebTestClient.ResponseSpec =
    webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          otherCourseName = otherCourseName,
          prisonNumber = prisonNumber,
        ),
      )
      .exchange()
}
