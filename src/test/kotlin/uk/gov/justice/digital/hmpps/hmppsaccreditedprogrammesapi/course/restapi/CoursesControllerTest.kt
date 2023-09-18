package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CoursesControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class CoursesControllerTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {
  private val repository = InMemoryCourseRepository()

  @MockkBean
  private lateinit var coursesService: CourseService

  @Nested
  inner class GetCoursesTests {
    @Test
    fun `get all courses`() {
      every { coursesService.allCourses() } returns repository.allCourses()

      mockMvc.get("/courses") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          this.contentType(MediaType.APPLICATION_JSON)
          this.json(
            """
          [
            { "name": "Lime Course", "alternateName": "LC", "referable": true },
            { "name": "Azure Course", "alternateName": "AC++", "referable": true },
            { "name": "Violet Course", "referable": true }
        ]
        """,
          )
        }
      }
    }

    @Test
    fun `get all courses - no token`() {
      mockMvc.get("/courses") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }

    @Test
    fun `get a course - happy path`() {
      val expectedCourse = repository.allCourses().first()
      every { coursesService.course(expectedCourse.id!!) } returns repository.allCourses().first()

      mockMvc.get("/courses/${expectedCourse.id}") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          json(
            """
            {
              "id": "${expectedCourse.id?.toString()}",
              "name": "Lime Course",
              "alternateName": "LC",
              "audiences": [],
              "coursePrerequisites": [
                { "name": "Setting", "description": "Custody"},
                { "name": "Risk criteria", "description":  "High ESARA/SARA/OVP, High OGRS"},
                { "name": "Criminogenic needs", "description": "Relationships, Thinking and Behaviour, Attitudes, Lifestyle" }
              ],
            }
            """,
          )
        }
      }
    }

    @Test
    fun `get a course - not found`() {
      val randomId = UUID.randomUUID()
      every { coursesService.course(randomId) } returns null

      mockMvc.get("/courses/$randomId") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isNotFound() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.status") { value(404) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { value("Not Found: No Course found at /courses/$randomId") }
          jsonPath("$.developerMessage") { value("No Course found at /courses/$randomId") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }
    }

    @Test
    fun `get a course - bad id`() {
      val courseId = "bad-id"

      mockMvc.get("/courses/$courseId") {
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
    fun `get a course - no token`() {
      mockMvc.get("/courses/${UUID.randomUUID()}") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class GetOfferingsTests {
    @Test
    fun `get all offerings for a course`() {
      val courseId = repository.allCourses().first().id!!
      every { coursesService.offeringsForCourse(courseId) } returns repository.offeringsForCourse(courseId)

      mockMvc.get("/courses/$courseId/offerings") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json(
            """
        [
          { "organisationId": "MDI", "contactEmail":"nobody-mdi@digital.justice.gov.uk" },
          { "organisationId": "BWN", "contactEmail":"nobody-bwn@digital.justice.gov.uk" },
          { "organisationId": "BXI", "contactEmail":"nobody-bxi@digital.justice.gov.uk" }
        ]
      """,
          )
        }
      }
    }

    @Test
    fun `get all offerings for a course - no token`() {
      mockMvc.get("/courses/${UUID.randomUUID()}/offerings") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class PutCoursesTests {
    @Test
    fun `put courses csv`() {
      every { coursesService.replaceAllCourses(any()) } just Runs

      mockMvc.put("/courses") {
        contentType = MediaType("text", "csv")
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = CsvTestData.coursesCsvText
      }.andExpect {
        status { isNoContent() }
      }

      verify { coursesService.replaceAllCourses(CsvTestData.newCourses) }
    }
  }

  @Nested
  inner class PutPrerequisitesTests {
    @Test
    fun `put prerequisites csv`() {
      every { coursesService.replaceAllPrerequisites(any()) } returns emptyList()

      mockMvc.put("/courses/prerequisites") {
        contentType = MediaType("text", "csv")
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = CsvTestData.prerequisitesCsvText
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.size()") { value(0) }
        }
      }

      verify { coursesService.replaceAllPrerequisites(CsvTestData.prerequisiteRecords.map(PrerequisiteRecord::toDomain)) }
    }
  }

  @Nested
  inner class PutOfferingsTests {
    @Test
    fun `put offerings csv`() {
      every { coursesService.updateOfferings(any<List<OfferingUpdate>>()) } returns emptyList()

      mockMvc.put("/courses/offerings") {
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
}
