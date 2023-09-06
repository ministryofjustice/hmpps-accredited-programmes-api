package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationAdded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CreateCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
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
    val courseId = getFirstCourseId()

    val cpa = webTestClient
      .post()
      .uri("/course-participation-history")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          prisonNumber = "A1234AA",
        ),
      ).exchange()
      .expectStatus().isCreated
      .expectBody(CourseParticipationAdded::class.java)
      .returnResult().responseBody!!

    cpa.shouldNotBeNull()
    cpa.id.shouldNotBeNull()

    val courseParticipation = webTestClient
      .get()
      .uri("/course-participation-history/{id}", cpa.id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(CourseParticipation::class.java)
      .returnResult().responseBody!!

    courseParticipation shouldBe CourseParticipation(
      id = cpa.id,
      courseId = courseId,
      prisonNumber = "A1234AA",
    )
  }

  @Test
  fun `Add a course participation history with courseId and otherCourseName is rejected`() {
    val courseId = getFirstCourseId()

    val errorResponse = webTestClient
      .post()
      .uri("/course-participation-history")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          otherCourseName = "A Course",
          prisonNumber = "A1234AA",
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
    val courseId = getFirstCourseId()

    val courseParticipationId = addCourseParticipationHistory(courseId, "A1234AA")

    webTestClient
      .put()
      .uri("/course-participation-history/{id}", courseParticipationId)
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
      ).exchange()
      .expectStatus().isNoContent

    val cp = getCourseParticipation(courseParticipationId)

    cp shouldBe CourseParticipation(
      id = courseParticipationId,
      courseId = courseId,
      yearStarted = 2020,
      setting = CourseSetting.custody,
      prisonNumber = "A1234AA",
      outcome = CourseParticipationOutcome(
        status = CourseParticipationOutcome.Status.deselected,
        detail = "Some detail",
      ),
    )
  }

  @Test
  fun `find course participation history by prison number returns matches`() {
    val ids = getCourseIds().map { addCourseParticipationHistory(it, "A1234AA") }.toSet()
    ids.shouldNotBeEmpty()

    val otherIds = getCourseIds().map { addCourseParticipationHistory(it, "Z9999ZZ") }.toSet()

    otherIds.intersect(ids).shouldBeEmpty()

    val courseIdsForPrisonNumber = webTestClient
      .get()
      .uri("/course-participation-history?prisonNumber={prisonNumber}", "A1234AA")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<List<CourseParticipation>>() {})
      .returnResult().responseBody!!.map { it.id }.toSet()

    courseIdsForPrisonNumber shouldBe ids
  }

  @Test
  fun `find course participation history by prison number returns no matches`() {
    getCourseIds().map { addCourseParticipationHistory(it, "A1234AA") }

    webTestClient
      .get()
      .uri("/course-participation-history?prisonNumber={prisonNumber}", "Z0000ZZ")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(object : ParameterizedTypeReference<List<CourseParticipation>>() {})
      .returnResult().responseBody!!.shouldBeEmpty()
  }

  private fun getFirstCourseId(): UUID = getCourseIds().first()

  private fun getCourseIds(): List<UUID> =
    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody(object : ParameterizedTypeReference<List<Course>>() {})
      .returnResult().responseBody!!.map { it.id }

  private fun addCourseParticipationHistory(courseId: UUID, prisonNumber: String): UUID =
    webTestClient
      .post()
      .uri("/course-participation-history")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        CreateCourseParticipation(
          courseId = courseId,
          prisonNumber = prisonNumber,
        ),
      ).exchange()
      .expectStatus().isCreated
      .expectBody(CourseParticipationAdded::class.java)
      .returnResult().responseBody!!.id

  private fun getCourseParticipation(id: UUID): CourseParticipation =
    webTestClient
      .get()
      .uri("/course-participation-history/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody(CourseParticipation::class.java)
      .returnResult().responseBody!!
}
