package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.*
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course as ApiCourse

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
          { "name": "Lime Course", "alternateName": "LC", "referable": true },
          { "name": "Azure Course", "alternateName": "AC++", "referable": true },
          { "name": "Violet Course", "referable": true }
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
  fun `get a course by offering id`() {
    webTestClient
      .get()
      .uri("/offerings/$courseOfferingId/course")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseId)
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
  fun `get a course offering using short url - happy path`() {
    webTestClient
      .get()
      .uri("/offerings/$courseOfferingId")
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
      .jsonPath("$.length()").isEqualTo(CsvTestData.newCourses.size)
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
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)

    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody()
      .jsonPath("$..coursePrerequisites.length()").isEqualTo(35)
  }

  @DirtiesContext
  @Test
  fun `put offerings csv`() {
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
      .uri("/courses/offerings")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType("text", "csv"))
      .bodyValue(CsvTestData.offeringsCsvText)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)

    val courses: List<ApiCourse> = webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody(object : ParameterizedTypeReference<List<ApiCourse>>() {})
      .returnResult().responseBody!!

    val allOfferings: List<List<CourseOffering>> = courses.map {
      webTestClient
        .get()
        .uri("/courses/${it.id}/offerings")
        .headers(jwtAuthHelper.authorizationHeaderConfigurer())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectBody(object : ParameterizedTypeReference<List<CourseOffering>>() {})
        .returnResult().responseBody!!
    }

    allOfferings shouldHaveSize CsvTestData.newCourses.size

    val actualOrganisationIds: Set<String> = allOfferings
      .flatMap { courseOfferings ->
        courseOfferings.map { offering -> offering.organisationId }
      }.toSet()

    val expectedOrganisationIds = CsvTestData.offeringsRecords.map { it.prisonId }.toSet()

    actualOrganisationIds shouldContainExactly expectedOrganisationIds
  }
}
