package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CoursesIntegrationTest
@Autowired constructor(
  val webTestClient: WebTestClient,
  val jwtAuthHelper: JwtAuthHelper,
) {
  companion object {
    const val courseId = "d3abc217-75ee-46e9-a010-368f30282367"
    const val courseOfferingId = "7fffcc6a-11f8-4713-be35-cf5ff1aee517"
  }

  @Test
  fun `get all courses`() {
    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
          [
            { "name": "Lime Course" },
            { "name": "Azure Course" },
            { "name": "Violet Course" }
        ]
        """,
      )
  }

  @Test
  fun `get a course - happy path`() {
    webTestClient
      .get()
      .uri("/courses/$courseId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseId)
  }

  @Test
  fun `get a course - not found`() {
    val randomId = UUID.randomUUID()

    webTestClient
      .get()
      .uri("/courses/$randomId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
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
  fun `get all offerings for a course`() {
    webTestClient
      .get()
      .uri("/courses/$courseId/offerings")
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
  fun `get a course offering - happy path`() {
    webTestClient
      .get()
      .uri("/courses/$courseId/offerings/$courseOfferingId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseOfferingId)
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
  }

  @Test
  fun `get a course offering - not found`() {
    val randomUuid = UUID.randomUUID()

    webTestClient
      .get()
      .uri("/courses/$randomUuid/offerings/$randomUuid")
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

  @DirtiesContext
  @Test
  fun `put courses csv`() {
    webTestClient
      .put()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType("text", "csv"))
      .bodyValue(CsvTestData.coursesCsvText)
      .exchange()
      .expectStatus().is2xxSuccessful

    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody()
      .jsonPath("$.length()").isEqualTo(16)
  }

  @DirtiesContext
  @Test
  fun `put prerequisites csv`() {
    webTestClient
      .put()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType("text", "csv"))
      .bodyValue(CsvTestData.coursesCsvText)
      .exchange()
      .expectStatus().is2xxSuccessful

    webTestClient
      .put()
      .uri("/courses/prerequisites")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType("text", "csv"))
      .bodyValue(CsvTestData.prerequisitesCsvText)
      .exchange()
      .expectStatus().is2xxSuccessful

    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody()
      .jsonPath("$..coursePrerequisites.length()").isEqualTo(94)
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
