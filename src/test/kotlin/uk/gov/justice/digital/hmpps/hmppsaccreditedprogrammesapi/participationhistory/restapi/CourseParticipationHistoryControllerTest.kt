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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.CoursesControllerTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
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
  private lateinit var service: CourseParticipationHistoryService

  @Nested
  inner class PostParticipationHistoryTests {
    @Test
    fun `add participation history`() {
      val uuid = UUID.randomUUID()
      val courseId = UUID.randomUUID()
      val courseParticipationSlot = slot<CourseParticipationHistory>()

      every { service.addCourseParticipation(capture(courseParticipationSlot)) } returns uuid

      mockMvc.post("/course-participation-history") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """{
          "courseId": "$courseId",
          "prisonNumber": "A1234AA",
          "yearStarted": 2020,
          "outcome": {
            "status": "complete",
            "detail": "Detail"
          },
          "setting": "custody",
          "source": "No idea"
        }
        """
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
        yearStarted = Year.of(2020),
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Detail",
        ),
        setting = CourseSetting.CUSTODY,
      )
    }

    @Test
    fun `add participation history - bad content`() {
      mockMvc.post("/course-participation-history") {
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
      mockMvc.post("/course-participation-history") {
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
        yearStarted = Year.of(2020),
        prisonNumber = "A1234BC",
        setting = CourseSetting.COMMUNITY,
        outcome = CourseOutcome(
          status = CourseStatus.DESELECTED,
          detail = "Detail",
        ),
      )

      mockMvc.get("/course-participation-history/{id}", participationHistoryId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json(
            """{ 
            "id": "$participationHistoryId",
            "otherCourseName": null,
            "courseId": "$courseId",
            "yearStarted": 2020,
            "prisonNumber": "A1234BC",
            "setting": "community",
            "outcome": {
                    "status": "deselected",
                    "detail": "Detail"
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

      mockMvc.get("/course-participation-history/{id}", participationHistoryId) {
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
    fun `get participation history by id - not a uuid`() {
      mockMvc.get("/course-participation-history/{id}", "abcd") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isBadRequest() }
        content {
          json(
            """{
              "status":400,
              "errorCode":null,
              "userMessage":"Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: abcd",
              "developerMessage":"Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: abcd",
              "moreInfo":null
              }""",
          )
        }
      }

      confirmVerified(service)
    }
  }
}
