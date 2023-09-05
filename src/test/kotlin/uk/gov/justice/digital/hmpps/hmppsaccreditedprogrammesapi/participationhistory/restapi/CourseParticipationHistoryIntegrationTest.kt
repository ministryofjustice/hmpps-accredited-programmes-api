package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
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

    courseParticipation shouldBeEqualToComparingFields CourseParticipation(
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

  private fun getFirstCourseId(): UUID =
    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody(object : ParameterizedTypeReference<List<Course>>() {})
      .returnResult().responseBody!![0].id
}
