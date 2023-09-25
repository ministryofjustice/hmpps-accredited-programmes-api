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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.factory.CourseParticipationHistoryEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.util.randomStringUpperCaseWithNumbers

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
  private lateinit var courseParticipationHistoryService: CourseParticipationHistoryService

  @Nested
  inner class FindByPrisonNumber {
    @Test
    fun `courseParticipationHistoryGet with JWT and valid prison number returns 200 with correct body`() {
      val sharedPrisonNumber = randomStringUpperCaseWithNumbers(6)

      val courseParticipationHistoryList = listOf(
        CourseParticipationHistoryEntityFactory().withPrisonNumber(sharedPrisonNumber).produce(),
        CourseParticipationHistoryEntityFactory().withPrisonNumber(sharedPrisonNumber).produce(),
      )

      every { courseParticipationHistoryService.findByPrisonNumber(any()) } returns courseParticipationHistoryList

      mockMvc.get("/people/$sharedPrisonNumber/course-participations") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          courseParticipationHistoryList.forEachIndexed { index, entity ->
            jsonPath("$[$index].id") { value(entity.id.toString()) }
            jsonPath("$[$index].prisonNumber") { value(sharedPrisonNumber) }
          }
        }
      }

      verify { courseParticipationHistoryService.findByPrisonNumber(sharedPrisonNumber) }
    }

    @Test
    fun `courseParticipationHistoryGet with JWT and no prison number returns 200 with an empty body`() {
      every { courseParticipationHistoryService.findByPrisonNumber(any()) } returns emptyList()

      mockMvc.get("/people/${randomStringUpperCaseWithNumbers(6)}/course-participations") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          jsonPath("$") { isEmpty() }
        }
      }
    }
  }
}