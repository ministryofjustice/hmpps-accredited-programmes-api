package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi

import com.ninjasquad.springmockk.MockkBean
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
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [PeopleControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class PeopleControllerTest(
  @Autowired
  val mockMvc: MockMvc,
  @Autowired
  val jwtAuthHelper: JwtAuthHelper,
) {
  @MockkBean
  private lateinit var service: CourseParticipationService

  @Nested
  inner class FindByPrisonNumber {
    @Test
    fun `GET course-participations with JWT and valid prison number returns 200 with correct body`() {
      val prisonNumber = randomPrisonNumber()
      val createdAt = LocalDateTime.now()
      val username = randomLowercaseString(10)

      val courseParticipations = listOf(
        CourseParticipation(
          id = UUID.randomUUID(),
          otherCourseName = null,
          courseId = UUID.randomUUID(),
          prisonNumber = prisonNumber,
          source = "Source of information 1",
          detail = "Course detail 1",
          setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = "A location"),
          outcome = CourseOutcome(status = CourseStatus.INCOMPLETE, yearStarted = Year.of(2018), yearCompleted = Year.of(2023)),
          createdByUsername = username,
          createdDateTime = createdAt,
        ),
        CourseParticipation(
          id = UUID.randomUUID(),
          otherCourseName = "A Course Name",
          courseId = null,
          prisonNumber = prisonNumber,
          source = "Source of information 2",
          detail = "Course detail 2",
          setting = CourseParticipationSetting(type = CourseSetting.CUSTODY),
          outcome = CourseOutcome(),
          createdByUsername = username,
          createdDateTime = createdAt,
        ),
      )

      every { service.findByPrisonNumber(any()) } returns courseParticipations

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
                "id": "${courseParticipations[1].id}",
                "otherCourseName": "A Course Name",
                "courseId": null,
                "prisonNumber": "$prisonNumber",
                "source": "Source of information 2",
                "detail": "Course detail 2",
                "setting": { "type": "custody", "location": null },
                "outcome": { "status":  null, "yearStarted":  null, "yearCompleted":  null },
                "addedBy": "$username",
                "createdAt": "${createdAt.format(DateTimeFormatter.ISO_DATE_TIME)}"
              }
            ]""",
          )
        }
      }

      verify { service.findByPrisonNumber("A1234AA") }
    }

    @Test
    fun `GET course-participations with JWT and unknown prison number returns 200 with an empty body`() {
      every { service.findByPrisonNumber(any()) } returns emptyList()

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
