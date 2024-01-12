package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationSettingFactory
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

  val objectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
    configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
    configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
  }

  @MockkBean
  private lateinit var courseParticipationService: CourseParticipationService

  @Nested
  inner class AddCourseParticipationTests {
    @Test
    fun `createCourseParticipation with JWT and valid payload with valid id returns 201 with correct body`() {
      val courseParticipationId = UUID.randomUUID()
      val courseParticipationSlot = slot<CourseParticipationEntity>()
      val courseParticipation = CourseParticipationEntityFactory()
        .withId(courseParticipationId)
        .withCourseName("Course name")
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.COMPLETE).withYearStarted(Year.of(2020)).produce())
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.CUSTODY).produce())
        .withCreatedByUsername(CLIENT_USERNAME)
        .produce()

      every { courseParticipationService.createCourseParticipation(capture(courseParticipationSlot)) } returns courseParticipation

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = objectMapper.writeValueAsString(courseParticipation)
      }.andExpect {
        status { isCreated() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.id") { value(courseParticipationId.toString()) }
      }

      verify { courseParticipationService.createCourseParticipation(any()) }

      courseParticipationSlot.captured.shouldBeEqualToIgnoringFields(courseParticipation, CourseParticipationEntity::id)
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
      val courseParticipation = CourseParticipationEntityFactory()
        .withCourseName("Course name")
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.COMPLETE).withYearStarted(Year.of(2020)).produce())
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.CUSTODY).produce())
        .withCreatedByUsername(CLIENT_USERNAME)
        .produce()

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        content = objectMapper.writeValueAsString(courseParticipation)
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
      val courseParticipation = CourseParticipationEntityFactory()
        .withCourseName("Course name")
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.CUSTODY).produce())
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.COMPLETE).produce())
        .produce()

      val modifiedOutcome = when (field) {
        "yearStarted" -> courseParticipation.outcome!!.copy(yearStarted = Year.of(invalidYear))
        "yearCompleted" -> courseParticipation.outcome!!.copy(yearCompleted = Year.of(invalidYear))
        else -> courseParticipation.outcome
      }
      val modifiedCourseParticipation = courseParticipation.copy(outcome = modifiedOutcome)

      mockMvc.post("/course-participations") {
        accept = MediaType.APPLICATION_JSON
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = objectMapper.writeValueAsString(modifiedCourseParticipation)
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
      val courseParticipation = CourseParticipationEntityFactory()
        .withId(courseParticipationId)
        .withCourseName("Course name")
        .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.COMMUNITY).produce())
        .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.INCOMPLETE).withYearStarted(Year.of(2020)).produce())
        .produce()

      every { courseParticipationService.getCourseParticipationById(any()) } returns courseParticipation

      mockMvc.get("/course-participations/{id}", courseParticipationId) {
        accept = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content { contentType(MediaType.APPLICATION_JSON) }
        jsonPath("$.courseName") { value(courseParticipation.courseName) }
        jsonPath("$.setting.type") { value(courseParticipation.setting!!.type.name.lowercase()) }
        jsonPath("$.outcome.status") { value(courseParticipation.outcome!!.status.name.lowercase()) }
        jsonPath("$.outcome.yearStarted") { value(courseParticipation.outcome!!.yearStarted?.value) }
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
