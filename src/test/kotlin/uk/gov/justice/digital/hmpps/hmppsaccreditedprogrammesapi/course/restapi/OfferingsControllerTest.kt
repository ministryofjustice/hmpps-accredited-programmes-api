package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
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
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toDomain
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
  private val repository = InMemoryCourseRepository()

  @MockkBean
  private lateinit var coursesService: CourseService

  @Test
  fun `get a course by offering id - happy path`() {
    val courseId = repository.allCourses().first().id
    val courseOfferingId = repository.offeringsForCourse(courseId!!).first().id

    every { coursesService.getCourseForOfferingId(any()) } returns repository.course(courseId)

    mockMvc.get(COURSE_BY_OFFERING_ID_TEMPLATE, courseOfferingId) {
      accept = MediaType.APPLICATION_JSON
      header(AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        jsonPath("$.id") { value(courseId.toString()) }
      }
    }
  }

  @Test
  fun `get a course by offering id - not found`() {
    val offeringId = UUID.randomUUID()
    every { coursesService.getCourseForOfferingId(any()) } returns null

    mockMvc.get(COURSE_BY_OFFERING_ID_TEMPLATE, offeringId) {
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
  fun `get a course by offering id - bad uuid`() {
    val offeringId = "bad-id"

    mockMvc.get("/offerings/$offeringId") {
      accept = MediaType.APPLICATION_JSON
      header(AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(400) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `get a course by offering id - no token`() {
    mockMvc.get(COURSE_BY_OFFERING_ID_TEMPLATE, UUID.randomUUID()) {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }

  @Nested
  inner class PutOfferingsTests {
    @Test
    fun `put offerings csv`() {
      every { coursesService.updateOfferings(any<List<OfferingUpdate>>()) } returns emptyList()

      mockMvc.put("/offerings/csv") {
        contentType = MediaType("text", "csv")
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = CsvTestData.offeringsCsvText
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.size()") { value(0) }
        }
      }

      verify { coursesService.updateOfferings(CsvTestData.offeringsRecords.map(OfferingRecord::toDomain)) }
    }
  }

  private companion object {
    private const val COURSE_BY_OFFERING_ID_TEMPLATE = "/offerings/{offeringId}/course"
  }
}
