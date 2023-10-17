package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class PeopleControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {
  @MockkBean
  private lateinit var service: CourseParticipationService

  @Nested
  inner class FindByPrisonNumber {
    @Test
    fun `getCourseParticipationsForPrisonNumber with JWT and valid prison number returns 200 with correct body`() {
      val prisonNumber = randomPrisonNumber()
      val createdAt = LocalDateTime.now()
      val username = randomLowercaseString(10)

      val courseParticipations = listOf(
        CourseParticipationEntity(
          courseName = "Course name 1",
          id = UUID.randomUUID(),
          otherCourseName = null,
          courseId = UUID.randomUUID(),
          prisonNumber = prisonNumber,
          source = "Source of information 1",
          detail = "Course detail 1",
          setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = "A location"),
          outcome = CourseParticipationOutcome(status = CourseStatus.INCOMPLETE, yearStarted = Year.of(2018), yearCompleted = Year.of(2023)),
          createdByUsername = username,
          createdDateTime = createdAt,
        ),
        CourseParticipationEntity(
          courseName = "Course name 2",
          id = UUID.randomUUID(),
          otherCourseName = "A Course Name",
          courseId = null,
          prisonNumber = prisonNumber,
          source = "Source of information 2",
          detail = "Course detail 2",
          setting = CourseParticipationSetting(type = CourseSetting.CUSTODY),
          outcome = CourseParticipationOutcome(status = CourseStatus.INCOMPLETE),
          createdByUsername = username,
          createdDateTime = createdAt,
        ),
      )

      every { service.getCourseParticipationsByPrisonNumber(any()) } returns courseParticipations

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        authorizationHeader()
      }.andExpect {
        status { isOk() }
        content {
          json(
            strict = true,
            jsonContent =
            """[
              {
                "courseName": "Course name 1",
                "id": "${courseParticipations[0].id}",
                "otherCourseName": null,
                "courseId": "${courseParticipations[0].courseId}",
                "prisonNumber": "$prisonNumber",
                "source": "Source of information 1",
                "detail": "Course detail 1",
                "setting": { "type": "community", "location": "A location" },
                "outcome": { "status": "incomplete", "yearStarted": 2018, "yearCompleted": 2023 },
                "addedBy": "$username",
                "createdAt": "${createdAt.format(DateTimeFormatter.ISO_DATE_TIME)}"
              },
              {
                "courseName": "Course name 2",
                "id": "${courseParticipations[1].id}",
                "otherCourseName": "A Course Name",
                "courseId": null,
                "prisonNumber": "$prisonNumber",
                "source": "Source of information 2",
                "detail": "Course detail 2",
                "setting": { "type": "custody", "location": null },
                "outcome": { "status": "incomplete", "yearStarted": null, "yearCompleted":  null },
                "addedBy": "$username",
                "createdAt": "${createdAt.format(DateTimeFormatter.ISO_DATE_TIME)}"
              }
            ]""",
          )
        }
      }

      verify { service.getCourseParticipationsByPrisonNumber("A1234AA") }
    }

    @Test
    fun `getCourseParticipationsForPrisonNumber with JWT and unknown prison number returns 200 with empty body`() {
      every { service.getCourseParticipationsByPrisonNumber(any()) } returns emptyList()

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        authorizationHeader()
      }.andExpect {
        status { isOk() }
        content {
          json("[]")
        }
      }
    }
  }

  private fun MockHttpServletRequestDsl.authorizationHeader() = header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
}
