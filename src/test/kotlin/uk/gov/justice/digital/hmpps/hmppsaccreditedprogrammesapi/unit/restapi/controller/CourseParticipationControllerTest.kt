package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import java.time.Year
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var courseParticipationService: CourseParticipationService

  @Nested
  inner class AddCourseParticipationTests {
    @Test
    fun `createCourseParticipation with JWT and valid payload with valid id returns 201 with correct body`() {
      val courseParticipationId = UUID.randomUUID()
      val courseParticipationSlot = slot<CourseParticipationEntity>()
      every { courseParticipationService.createCourseParticipation(capture(courseParticipationSlot)) } returns
        CourseParticipationEntity(
          id = courseParticipationId,
          courseName = "Course name",
          source = "Source of information",
          detail = "Course detail",
          prisonNumber = "A1234AA",
          outcome = CourseParticipationOutcome(status = CourseStatus.COMPLETE, yearStarted = Year.of(2020)),
          setting = CourseParticipationSetting(type = CourseSetting.CUSTODY),
          createdByUsername = CLIENT_USERNAME,
        )
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """
          { 
            "id": "$courseParticipationId",
            "courseName": "Course name",
            "prisonNumber": "A1234AA",
            "source": "Source of information",
            "detail": "Course detail",
            "setting": {
              "type": "custody"
            },
            "outcome": {
              "status": "complete",
              "yearStarted": 2020
            }
          }"""
      }.andExpect {
        status { isCreated() }
        content {
          json(""" { "id": "$courseParticipationId" } """)
        }
      }
      verify { courseParticipationService.createCourseParticipation(any()) }
      courseParticipationSlot.captured shouldBeEqualToComparingFields CourseParticipationEntity(
        courseName = "Course name",
        prisonNumber = "A1234AA",
        source = "Source of information",
        detail = "Course detail",
        outcome = CourseParticipationOutcome(
          status = CourseStatus.COMPLETE,
          yearStarted = Year.of(2020),
        ),
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY),
        createdByUsername = CLIENT_USERNAME,
      )
    }

    @Test
    fun `createCourseParticipation with JWT and invalid payload returns 400 with error body`() {
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
    fun `createCourseParticipation without JWT returns 401`() {
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        content = """
          { 
            "courseName": "Course name",
            "prisonNumber": "A1234AA",
            "source": "Source of information",
            "detail": "Course detail",
            "setting": {
              "type": "custody"
            },
            "outcome": {
              "status": "complete",
              "yearStarted": 2020
            }
          }"""
      }.andExpect {
        status { isUnauthorized() }
      }
    }

    @ParameterizedTest
    @CsvSource(
      "yearStarted, 1989",
      "yearCompleted, 1985",
    )
    fun `createCourseParticipation with invalid year fields returns 400 with validation error message`(
      field: String,
      invalidYear: Int,
    ) {
      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = """
          { 
            "courseName": "Course name",
            "prisonNumber": "A1234AA",
            "source": "Source of information",
            "detail": "Course detail",
            "setting": {
              "type": "custody"
            },
            "outcome": {
              "status": "complete",
              "$field": $invalidYear
            }
          }"""
      }.andExpect {
        status { isBadRequest() }
        content {
          jsonPath("$.status") { value(400) }
          jsonPath("$.userMessage") { value("Validation failure: $field is not valid.") }
          jsonPath("$.developerMessage") { value("$field is not valid.") }
        }
      }
    }
  }

  @Nested
  inner class GetCourseParticipationTests {
    @Test
    fun `getCourseParticipationById with JWT returns 200 with correct body`() {
      val courseParticipationId = UUID.randomUUID()

      every { courseParticipationService.getCourseParticipationById(any()) } returns CourseParticipationEntity(
        id = courseParticipationId,
        courseName = "Course name",
        prisonNumber = "A1234BC",
        source = "Source of information",
        detail = "Course detail",
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY),
        outcome = CourseParticipationOutcome(
          status = CourseStatus.INCOMPLETE,
          yearStarted = Year.of(2020),
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
              "courseName": "Course name",
              "prisonNumber": "A1234BC",
              "source": "Source of information",
              "detail": "Course detail",
              "setting": {
                type: "community"
              },
              "outcome": {
                "status": "incomplete",
                "yearStarted": 2020
              }
            }""",
          )
        }
      }

      verify { courseParticipationService.getCourseParticipationById(courseParticipationId) }
    }

    @Test
    fun `getCourseParticipationById with random UUID returns 404 with error body`() {
      val courseParticipationId = UUID.randomUUID()

      every { courseParticipationService.getCourseParticipationById(any()) } returns null

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

      verify { courseParticipationService.getCourseParticipationById(courseParticipationId) }
    }

    @Test
    fun `getCourseParticipationById with invalid UUID returns 400 with error body`() {
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
