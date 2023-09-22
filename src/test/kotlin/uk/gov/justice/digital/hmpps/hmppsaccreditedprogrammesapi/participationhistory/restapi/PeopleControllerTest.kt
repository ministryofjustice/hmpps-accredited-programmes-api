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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import java.util.*

@WebMvcTest
@ContextConfiguration(classes = [PeopleControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config",
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
  private lateinit var service: CourseParticipationHistoryService

  @Nested
  inner class FindByPrisonNumber {
    @Test
    fun `it returns an array of results when course participations are found`() {
      val participationHistoryId = UUID.randomUUID()
      val courseId = UUID.randomUUID()

      every { service.findByPrisonNumber(any()) } returns listOf(
        CourseParticipationHistory(
          id = participationHistoryId,
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
        ),
      )

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          json(
            """
            [{
              "id": "$participationHistoryId",
              "otherCourseName": null,
              "courseId": "$courseId",
              "prisonNumber": "A1234BC",
              "source": "source",
              "setting": {
                "type": "community"
              },
              "outcome": {
                "status": "incomplete",
                "detail": "Detail",
                "yearStarted": 2020
              }
            }]""",
          )
        }
      }

      verify { service.findByPrisonNumber("A1234AA") }
    }

    @Test
    fun `it returns an empty array when no course participations are found`() {
      every { service.findByPrisonNumber(any()) } returns emptyList()

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
