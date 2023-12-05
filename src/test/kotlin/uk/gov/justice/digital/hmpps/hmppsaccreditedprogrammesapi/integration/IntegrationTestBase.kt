package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import java.util.UUID

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  fun getAllCourses(): List<Course> =
    webTestClient
      .get()
      .uri("/courses")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Course>>()
      .returnResult().responseBody!!

  fun getCourseById(courseId: UUID): Course =
    webTestClient
      .get()
      .uri("/courses/$courseId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Course>()
      .returnResult().responseBody!!

  fun getCourseIds(): List<UUID> = getAllCourses().map { it.id }

  fun getFirstCourseId(): UUID = getCourseIds().first()

  fun getAllOfferingsForCourse(courseId: UUID): List<CourseOffering> =
    webTestClient
      .get()
      .uri("/courses/$courseId/offerings")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseOffering>>()
      .returnResult().responseBody!!

  fun getFirstOfferingIdForCourse(courseId: UUID) = getAllOfferingsForCourse(courseId).first().id
}
