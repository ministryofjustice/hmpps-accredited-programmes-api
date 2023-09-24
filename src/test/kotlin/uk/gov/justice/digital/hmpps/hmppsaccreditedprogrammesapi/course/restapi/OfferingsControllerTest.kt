package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [OfferingsControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class OfferingsControllerTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var coursesService: CourseService

  @Test
  fun `offeringsIdCourseGet with JWT returns 200 with correct body`() {
    val offering = OfferingEntityFactory().withId(UUID.randomUUID()).produce()
    val course = CourseEntityFactory().withId(UUID.randomUUID()).withMutableOfferings(mutableSetOf(offering)).produce()

    every { coursesService.getCourseForOfferingId(any()) } returns course

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
  fun `offeringsIdCourseGet with random UUID returns 404 with error body`() {
    val offeringId = UUID.randomUUID()

    every { coursesService.getCourseForOfferingId(any()) } returns null

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
  fun `offeringsIdCourseGet with invalid UUID returns 400 with error body`() {
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
  fun `offeringsIdCourseGet without JWT returns 401`() {
    mockMvc.get("/offerings/${UUID.randomUUID()}/course") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }
}
