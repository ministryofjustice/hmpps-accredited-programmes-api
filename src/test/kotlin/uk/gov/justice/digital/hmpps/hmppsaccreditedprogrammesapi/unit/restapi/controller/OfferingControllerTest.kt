package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OfferingControllerTest@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var courseService: CourseService

  @Test
  fun `getCourseByOfferingId with JWT returns 200 with correct body`() {
    val offering = OfferingEntityFactory().withId(UUID.randomUUID()).produce()
    val course = CourseEntityFactory().withId(UUID.randomUUID()).withOfferings(mutableSetOf(offering)).produce()

    every { courseService.getCourseByOfferingId(any()) } returns course

    mockMvc.get("/offerings/${offering.id}/course") {
      accept = MediaType.APPLICATION_JSON
      header(AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        jsonPath("$.id") { value(course.id.toString()) }
      }
    }
  }

  @Test
  fun `getCourseByOfferingId with random UUID returns 404 with error body`() {
    val offeringId = UUID.randomUUID()

    every { courseService.getCourseByOfferingId(any()) } returns null

    mockMvc.get("/offerings/$offeringId/course") {
      accept = MediaType.APPLICATION_JSON
      header(AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNotFound() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(404) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { value("Not Found: No Course found at /offerings/$offeringId/course") }
        jsonPath("$.developerMessage") { value("No Course found at /offerings/$offeringId/course") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `getCourseByOfferingId with invalid UUID returns 400 with error body`() {
    val badId = "bad-id"

    mockMvc.get("/offerings/$badId/course") {
      accept = MediaType.APPLICATION_JSON
      header(AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(400) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: $badId") }
        jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: $badId") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `getCourseByOfferingId without JWT returns 401`() {
    mockMvc.get("/offerings/${UUID.randomUUID()}/course") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }
}
