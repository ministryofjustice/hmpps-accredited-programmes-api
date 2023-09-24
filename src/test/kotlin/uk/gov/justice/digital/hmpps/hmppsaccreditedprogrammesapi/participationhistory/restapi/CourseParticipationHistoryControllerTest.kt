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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CourseParticipationHistoryControllerTest::class])
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
  private lateinit var service: CourseParticipationHistoryService

  @Nested
  inner class AddParticipationHistoryTests {
    @Test
    fun `add participation history`() {
      val uuid = UUID.randomUUID()
      val courseId = UUID.randomUUID()
      val courseParticipationSlot = slot<CourseParticipationHistory>()

      every { service.addCourseParticipation(capture(courseParticipationSlot)) } returns
        CourseParticipationHistory(
          id = uuid,
          courseId = courseId,
          source = "source",
          prisonNumber = "A1234AA",
          outcome = CourseOutcome(
            status = CourseStatus.COMPLETE,
            detail = "Detail",
            yearStarted = Year.of(2020),
            yearCompleted = Year.of(2021),
          ),
          setting = CourseParticipationSetting(
            type = CourseSetting.CUSTODY,
            location = "location",
          ),
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
              "type": "custody",
              "location": "location"
            },
            "outcome": {
              "status": "complete",
              "detail": "Detail",
              "yearStarted": 2020,
              "yearCompleted": 2021
            }
          }"""
      }.andExpect {
        status { isCreated() }
        content {
          json(""" { "id": "$uuid" } """)
        }
      }

      verify { service.addCourseParticipation(any()) }

      courseParticipationSlot.captured shouldBeEqualToComparingFields CourseParticipationHistory(
        courseId = courseId,
        otherCourseName = null,
        prisonNumber = "A1234AA",
        source = "source",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Detail",
          yearStarted = Year.of(2020),
          yearCompleted = Year.of(2021),
        ),
        setting = CourseParticipationSetting(
          type = CourseSetting.CUSTODY,
          location = "location",
        ),
      )
    }

    @Test
    fun `add participation history - bad content`() {
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """asdfadsfasd"""
      }.andExpect {
        status { isBadRequest() }
        content {
          json(
            """{
            "status":400,
            "errorCode":null,
            "userMessage":"JSON parse error: Unrecognized token 'asdfadsfasd': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')",
            "developerMessage":"JSON parse error: Unrecognized token 'asdfadsfasd': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')",
            "moreInfo":null
            }""",
          )
        }
      }
    }

    @Test
    fun ` participation history - bad courseId`() {
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """{
          "courseId": "xxxxx",
          "prisonNumber": "A1234AA"
        }
        """
      }.andExpect {
        status { isBadRequest() }
        content {
          json(
            """{
            "status":400,
            "errorCode":null,
            "userMessage":"JSON parse error: Cannot deserialize value of type `java.util.UUID` from String \"xxxxx\": UUID has to be represented by standard 36-char representation",
            "developerMessage":"JSON parse error: Cannot deserialize value of type `java.util.UUID` from String \"xxxxx\": UUID has to be represented by standard 36-char representation",
            "moreInfo":null
            }""",
          )
        }
      }
    }
  }

  @Nested
  inner class GetParticipationHistoryTests {
    @Test
    fun `get participation history by id`() {
      val participationHistoryId = UUID.randomUUID()
      val courseId = UUID.randomUUID()

      every { service.getCourseParticipationHistory(any()) } returns CourseParticipationHistory(
        id = participationHistoryId,
        otherCourseName = null,
        courseId = courseId,
        prisonNumber = "A1234BC",
        source = "source",
        setting = CourseParticipationSetting(
          type = CourseSetting.COMMUNITY,
          location = "location",
        ),
        outcome = CourseOutcome(
          status = CourseStatus.INCOMPLETE,
          detail = "Detail",
          yearStarted = Year.of(2020),
          yearCompleted = Year.of(2020),
        ),
      )

      mockMvc.get("/course-participations/{id}", participationHistoryId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json(
            """
            { 
              "id": "$participationHistoryId",
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

      verify { service.getCourseParticipationHistory(participationHistoryId) }
    }

    @Test
    fun `get participation history by id - not found`() {
      val participationHistoryId = UUID.randomUUID()

      every { service.getCourseParticipationHistory(any()) } returns null

      mockMvc.get("/course-participations/{id}", participationHistoryId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isNotFound() }
        content {
          json(
            """{
              "status":404,
              "errorCode":null,
              "userMessage":"Not Found: No course participation history found for id $participationHistoryId",
              "developerMessage":"No course participation history found for id $participationHistoryId",
              "moreInfo":null
              }""",
          )
        }
      }

      verify { service.getCourseParticipationHistory(participationHistoryId) }
    }

    @Test
    fun `get participation history by id - bad uuid`() {
      val historicCourseParticipationId = "bad-id"

      mockMvc.get("/course-participations/$historicCourseParticipationId") {
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

      confirmVerified(service)
    }
  }
}
