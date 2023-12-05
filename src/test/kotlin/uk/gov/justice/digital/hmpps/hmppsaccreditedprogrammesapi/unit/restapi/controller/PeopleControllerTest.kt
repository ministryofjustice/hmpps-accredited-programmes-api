package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.UUID

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
  private lateinit var courseParticipationService: CourseParticipationService

  @Nested
  inner class FindByPrisonNumber {
    @Test
    fun `getCourseParticipationsByPrisonNumber with JWT and valid prison number returns 200 with correct body`() {
      val prisonNumber = randomPrisonNumber()
      val createdAt = LocalDateTime.now()
      val username = randomLowercaseString(10)

      val courseParticipations = listOf(
        CourseParticipationEntity(
          courseName = "Course name 1",
          id = UUID.randomUUID(),
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
          prisonNumber = prisonNumber,
          source = "Source of information 2",
          detail = "Course detail 2",
          setting = CourseParticipationSetting(type = CourseSetting.CUSTODY),
          outcome = CourseParticipationOutcome(status = CourseStatus.INCOMPLETE),
          createdByUsername = username,
          createdDateTime = createdAt,
        ),
      )

      every { courseParticipationService.getCourseParticipationsByPrisonNumber(any()) } returns courseParticipations

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
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

      verify { courseParticipationService.getCourseParticipationsByPrisonNumber("A1234AA") }
    }

    @Test
    fun `getCourseParticipationsByPrisonNumber with JWT and unknown prison number returns 200 with empty body`() {
      every { courseParticipationService.getCourseParticipationsByPrisonNumber(any()) } returns emptyList()

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json("[]")
        }
      }
    }
  }
}
