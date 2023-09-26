package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.CoursesControllerTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CoursesControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class CourseParticipationHistoryControllerTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {
  @MockkBean
  private lateinit var courseParticipationHistoryService: CourseParticipationHistoryService

  @Nested
  inner class AddParticipationHistoryTests {
    @Test
    fun `courseParticipationHistoryPost with JWT and valid payload with valid id returns 201 with correct body`() {
      val courseParticipationHistory = CourseParticipationHistoryEntityFactory()
        .withId(UUID.randomUUID())
        .withSource("source")
        .withOutcome(CourseOutcome(status = CourseStatus.COMPLETE, detail = "Detail"))
        .withSetting(CourseSetting.CUSTODY)
        .produce()

      every { courseParticipationHistoryService.addCourseParticipation(any()) } returns courseParticipationHistory

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = jacksonObjectMapper()
          .registerModule(JavaTimeModule())
          .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
          .writeValueAsString(courseParticipationHistory)
      }.andExpect {
        status { isCreated() }
        content {
          jsonPath("$.id") { value(courseParticipationHistory.id.toString()) }
          jsonPath("$.courseId") { value(courseParticipationHistory.courseId.toString()) }
          jsonPath("$.prisonNumber") { value(courseParticipationHistory.prisonNumber) }
          jsonPath("$.yearStarted") { value(courseParticipationHistory.yearStarted) }
          jsonPath("$.source") { value(courseParticipationHistory.source) }
          jsonPath("$.outcome.status") { value(courseParticipationHistory.outcome?.status?.name?.lowercase()) }
          jsonPath("$.outcome.detail") { value(courseParticipationHistory.outcome?.detail) }
          jsonPath("$.setting") { value(courseParticipationHistory.setting?.name?.lowercase()) }
        }
      }

      verify { courseParticipationHistoryService.addCourseParticipation(any()) }
    }

    @Test
    fun `courseParticipationHistoryPost with JWT and invalid payload returns 400 with error body`() {
      val badPayload = "bad_payload"

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = badPayload
      }.andExpect {
        status { isBadRequest() }
        content {
          jsonPath("$.status") { value(400) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { prefix("JSON parse error: Unrecognized token '$badPayload': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')") }
          jsonPath("$.developerMessage") { prefix("JSON parse error: Unrecognized token '$badPayload': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false'))") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }
    }

    @Test
    fun `courseParticipationHistoryPost without JWT returns 401`() {
      val courseParticipationHistory = CourseParticipationHistoryEntityFactory().produce()

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        content = jacksonObjectMapper().writeValueAsString(courseParticipationHistory)
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class GetParticipationHistoryTests {
    @Test
    fun `courseParticipationHistoryHistoricCourseParticipationIdGet with JWT returns 200 with correct body`() {
      val participationHistory = CourseParticipationHistoryEntityFactory().produce()

      every { courseParticipationHistoryService.getCourseParticipationHistory(any()) } returns participationHistory

      mockMvc.get("/course-participations/${participationHistory.id}") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          jsonPath("$.id") { value(participationHistory.id.toString()) }
        }
      }

      verify { courseParticipationHistoryService.getCourseParticipationHistory(participationHistory.id!!) }
    }

    @Test
    fun `courseParticipationHistoryHistoricCourseParticipationIdGet with random UUID returns 404 with error body`() {
      val randomId = UUID.randomUUID()

      every { courseParticipationHistoryService.getCourseParticipationHistory(any()) } returns null

      mockMvc.get("/course-participations/$randomId") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isNotFound() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.status") { value(404) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { value("Not Found: No course participation history found for id $randomId") }
          jsonPath("$.developerMessage") { value("No course participation history found for id $randomId") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }

      verify { courseParticipationHistoryService.getCourseParticipationHistory(randomId) }
    }

    @Test
    fun `courseParticipationHistoryHistoricCourseParticipationIdGet with invalid UUID returns 400 with error body`() {
      val badId = "bad-id"

      mockMvc.get("/course-participations/$badId") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
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

      confirmVerified(courseParticipationHistoryService)
    }
  }
}
