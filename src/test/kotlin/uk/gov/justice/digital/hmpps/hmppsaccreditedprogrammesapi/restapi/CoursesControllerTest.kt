package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CoursesControllerTest(
  @Autowired val webTestClient: WebTestClient,
  @Autowired val coursesService: CourseService,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {

  @Test
  fun `get all courses`() {
    webTestClient.get().uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
          [
            { "name": "Thinking Skills Programme" },
            { "name": "New me strengths" },
            { "name": "Becoming new me +" }
        ]
        """,
      )
  }

  @Test
  fun `get all courses - no token`() {
    webTestClient.get().uri("/courses")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectUnauthenticatedResponse()
  }

  @Test
  fun `get a course - happy path`() {
    val expectedCourse = coursesService.allCourses().first()

    webTestClient.get().uri("/courses/${expectedCourse.id}")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(expectedCourse.id.toString())
  }

  @Test
  fun `get a course - not found`() {
    val courseId = UUID.randomUUID()
    webTestClient.get().uri("/courses/$courseId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.status").isEqualTo(404)
      .jsonPath("$.errorCode").isEmpty
      .jsonPath("$.userMessage").value(startsWith("Not Found: No Course found at /courses/$courseId"))
      .jsonPath("$.developerMessage").value(startsWith("No Course found at /courses/$courseId"))
      .jsonPath("$.moreInfo").isEmpty
  }

  @Test
  fun `get a course - no token`() {
    val expectedCourse = coursesService.allCourses().first()

    webTestClient.get().uri("/courses/${expectedCourse.id}")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectUnauthenticatedResponse()
  }

  @Test
  fun `get all offerings for a course`() {
    val courseId = coursesService.allCourses().first().id

    webTestClient.get().uri("/courses/$courseId/offerings")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
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
          { "organisationId": "BXI", "contactEmail":"nobody-bxi@digital.justice.gov.uk" }
        ]
      """,
      )
  }

  @Test
  fun `get all offerings for a course - no token`() {
    webTestClient.get().uri("/courses/${UUID.randomUUID()}/offerings")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectUnauthenticatedResponse()
  }

  @Test
  fun `get a course offering - happy path`() {
    val courseId = coursesService.allCourses().first().id
    val courseOfferingId = coursesService.offeringsForCourse(courseId).first().id

    webTestClient.get().uri("/courses/$courseId/offerings/$courseOfferingId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseOfferingId.toString())
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
  }

  @Test
  fun `get a course offering - not found`() {
    val randomUuid = UUID.randomUUID()

    webTestClient.get().uri("/courses/$randomUuid/offerings/$randomUuid")
      .accept(MediaType.APPLICATION_JSON)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.status").isEqualTo(404)
      .jsonPath("$.errorCode").isEmpty
      .jsonPath("$.userMessage").value(startsWith("Not Found: No CourseOffering  found at /courses/"))
      .jsonPath("$.developerMessage").value(startsWith("No CourseOffering  found at /courses/"))
      .jsonPath("$.moreInfo").isEmpty
  }

  @Test
  fun `get a course offering - no token`() {
    val randomUuid = UUID.randomUUID()

    webTestClient.get().uri("/courses/$randomUuid/offerings/$randomUuid")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectUnauthenticatedResponse()
  }
}

private fun (WebTestClient.ResponseSpec).expectUnauthenticatedResponse(): WebTestClient.ResponseSpec {
  this.expectStatus().isUnauthorized
    .expectHeader().contentType("application/problem+json;charset=UTF-8")
    .expectBody()
    .json(
      """
      {
        "title": "Unauthenticated",
        "status": 401,
        "detail": "A valid HMPPS Auth JWT must be supplied via bearer authentication to access this endpoint"
      } 
      """,
    )
  return this
}
