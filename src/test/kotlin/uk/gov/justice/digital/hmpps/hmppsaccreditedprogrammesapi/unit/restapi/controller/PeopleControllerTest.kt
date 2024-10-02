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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationSettingFactory
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
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
      val createdAt = LocalDateTime.now()
      val cp1 = CourseParticipationEntityFactory()
        .withCourseName("Course name 1")
        .withPrisonNumber(PRISON_NUMBER_1)
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.COMMUNITY).withLocation("Location").produce())
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.COMPLETE).withYearStarted(Year.of(2018)).withYearCompleted(Year.of(2023)).produce())
        .withCreatedDateTime(createdAt)
        .produce()
      val cp2 = CourseParticipationEntityFactory()
        .withCourseName("Course name 2")
        .withPrisonNumber(PRISON_NUMBER_1)
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.CUSTODY).produce())
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.INCOMPLETE).produce())
        .withCreatedDateTime(createdAt)
        .produce()
      val courseParticipations = listOf(cp1, cp2)

      every { courseParticipationService.getCourseParticipationsByPrisonNumber(any()) } returns courseParticipations

      mockMvc.get("/people/{prisonNumber}/course-participations", "A1234AA") {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }

        courseParticipations.forEachIndexed { index, cp ->
          jsonPath("$[$index].courseName") { value(cp.courseName) }
          jsonPath("$[$index].id") { value(cp.id.toString()) }
          jsonPath("$[$index].prisonNumber") { value(cp.prisonNumber) }
          jsonPath("$[$index].setting.type") { value(cp.setting!!.type.name.lowercase()) }
          jsonPath("$[$index].outcome.status") { value(cp.outcome!!.status.name.lowercase()) }
          jsonPath("$[$index].addedBy") { value(REFERRER_USERNAME) }
          jsonPath("$[$index].createdAt") { value(createdAt.format(DateTimeFormatter.ISO_DATE_TIME)) }
        }

        jsonPath("$[0].setting.location") { value(cp1.setting!!.location) }
        jsonPath("$[0].outcome.yearStarted") { value(cp1.outcome!!.yearStarted?.value) }
        jsonPath("$[0].outcome.yearCompleted") { value(cp1.outcome!!.yearCompleted?.value) }

        jsonPath("$[1].setting.location") { doesNotExist() }
        jsonPath("$[1].outcome.yearStarted") { doesNotExist() }
        jsonPath("$[1].outcome.yearCompleted") { doesNotExist() }
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
