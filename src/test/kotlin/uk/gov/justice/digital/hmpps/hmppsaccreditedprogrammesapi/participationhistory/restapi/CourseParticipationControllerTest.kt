package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CourseParticipationControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class CourseParticipationControllerTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {
  @MockkBean
  private lateinit var courseParticipationService: CourseParticipationService

  @Nested
  inner class AddCourseParticipationTests {
    @Test
    fun `POST course participation with JWT and valid payload with valid id returns 201 with correct body`() {
      val uuid = UUID.randomUUID()
      val courseId = UUID.randomUUID()
      val courseParticipationSlot = slot<CourseParticipation>()
      every { courseParticipationService.addCourseParticipation(capture(courseParticipationSlot)) } returns
        CourseParticipation(
          id = uuid,
          courseId = courseId,
          source = "source",
          prisonNumber = "A1234AA",
          outcome = CourseOutcome(status = CourseStatus.COMPLETE, detail = "Detail"),
          setting = CourseSetting.CUSTODY,
          yearStarted = Year.of(2020),
          otherCourseName = null,
        )
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """
          { 
            "otherCourseName": null,
            "courseId": "$courseId",
            "prisonNumber": "A1234AA",
            "source": "source",
            "setting": {
              "type": "custody"
            },
            "outcome": {
              "status": "complete",
              "detail": "Detail",
              "yearStarted": 2020
            }
          }"""
      }.andExpect {
        status { isCreated() }
        content {
          json(""" { "id": "$uuid" } """)
        }
      }
      verify { courseParticipationService.addCourseParticipation(any()) }
      courseParticipationSlot.captured shouldBeEqualToComparingFields CourseParticipation(
        courseId = courseId,
        otherCourseName = null,
        prisonNumber = "A1234AA",
        yearStarted = Year.of(2020),
        source = "source",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Detail",
        ),
        setting = CourseSetting.CUSTODY,
      )
    }

    @Test
    fun `POST course participation with JWT and invalid payload returns 400 with error body`() {
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
    fun `POST course participation without JWT returns 401`() {
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        content = """
          { 
            "otherCourseName": null,
            "courseId": "${UUID.randomUUID()}",
            "prisonNumber": "A1234AA",
            "source": "source",
            "setting": {
              "type": "custody"
            },
            "outcome": {
              "status": "complete",
              "detail": "Detail",
              "yearStarted": 2020
            }
          }"""
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class GetCourseParticipationTests {
    @Test
    fun `GET course participation with JWT returns 200 with correct body`() {
      val courseParticipationId = UUID.randomUUID()
      val courseId = UUID.randomUUID()

      every { courseParticipationService.getCourseParticipation(any()) } returns CourseParticipation(
        id = courseParticipationId,
        otherCourseName = null,
        courseId = courseId,
        yearStarted = Year.of(2020),
        prisonNumber = "A1234BC",
        source = "source",
        setting = CourseSetting.COMMUNITY,
        outcome = CourseOutcome(
          status = CourseStatus.INCOMPLETE,
          detail = "Detail",
        ),
      )

      mockMvc.get("/course-participations/{id}", courseParticipationId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json(
            """
            { 
              "id": "$courseParticipationId",
              "otherCourseName": null,
              "courseId": "$courseId",
              "prisonNumber": "A1234BC",
              "source": "source",
              "setting": {
                type: "community"
              },
              "outcome": {
                "status": "incomplete",
                "detail": "Detail",
                "yearStarted": 2020
              }
            }""",
          )
        }
      }

      verify { courseParticipationService.getCourseParticipation(courseParticipationId) }
    }

    @Test
    fun `GET course participation with random UUID returns 404 with error body`() {
      val courseParticipationId = UUID.randomUUID()

      every { courseParticipationService.getCourseParticipation(any()) } returns null

      mockMvc.get("/course-participations/{id}", courseParticipationId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isNotFound() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.status") { value(404) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { value("Not Found: No course participation found for id $courseParticipationId") }
          jsonPath("$.developerMessage") { value("No course participation found for id $courseParticipationId") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }

      verify { courseParticipationService.getCourseParticipation(courseParticipationId) }
    }

    @Test
    fun `GET course participation with invalid UUID returns 400 with error body`() {
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

      confirmVerified(courseParticipationService)
    }
  }
}
