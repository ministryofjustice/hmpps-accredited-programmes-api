package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import org.hamcrest.Matchers.matchesRegex
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseService
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CoursesControllerTest(
  @Autowired val webTestClient: WebTestClient,
  @Autowired val coursesService: CourseService,
) {

  @Test
  fun `get a course offering - happy path`() {
    val courseId = coursesService.allCourses().first().id
    val courseOfferingId = coursesService.offeringsForCourse(courseId).first().id

    webTestClient.get().uri("/courses/$courseId/offerings/$courseOfferingId")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseOfferingId.toString())
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
      .jsonPath("$.duration").value(matchesRegex("""^P\d*(T\d+[H])?$"""))
      .jsonPath("$.groupSize").isNumber
  }

  @Test
  fun `get a course offering - not found`() {
    val randomUuid = UUID.randomUUID()

    webTestClient.get().uri("/courses/$randomUuid/offerings/$randomUuid")
      .accept(MediaType.APPLICATION_JSON)
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
}
