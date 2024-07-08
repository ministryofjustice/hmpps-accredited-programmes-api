package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.MEDIA_TYPE_TEXT_CSV
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.generateCourseRecords
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.generateOfferingRecords
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.generatePrerequisiteRecords
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.toCourseCsv
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.toOfferingCsv
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.toPrerequisiteCsv
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PrerequisiteEntityFactory
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var courseService: CourseService

  @Nested
  inner class GetCoursesTests {
    @Test
    fun `getAllCourses with JWT returns 200 with correct body`() {
      val courses = listOf(
        CourseEntityFactory().withName("Course1").produce(),
        CourseEntityFactory().withName("Course2").produce(),
        CourseEntityFactory().withName("Course3").produce(),
      )

      every { courseService.getAllCourses() } returns courses

      mockMvc.get("/courses") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          courses.forEachIndexed { index, course ->
            jsonPath("$[$index].name") { value(course.name) }
          }
        }
      }
    }

    @Test
    fun `getAllCourses without JWT returns 401`() {
      mockMvc.get("/courses") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }

    @Test
    fun `getAllCourseNames with JWT returns 200 with correct body`() {
      val offering1 = OfferingEntityFactory().withOrganisationId("OF1").withContactEmail("of1@digital.justice.gov.uk").produce()
      val offering2 = OfferingEntityFactory().withOrganisationId("OF2").withContactEmail("of2@digital.justice.gov.uk").produce()
      val offering3 = OfferingEntityFactory().withOrganisationId("OF3").withContactEmail("of3@digital.justice.gov.uk").produce()

      val courses = listOf(
        CourseEntityFactory().withName("Course1").withOfferings(mutableSetOf(offering1)).produce(),
        CourseEntityFactory().withName("Course1").withOfferings(mutableSetOf(offering2)).produce(),
        CourseEntityFactory().withName("Course1").withOfferings(mutableSetOf(offering3)).produce(),
        CourseEntityFactory().withName("Course2").produce(),
        CourseEntityFactory().withName("Course3").produce(),
      )
      every { courseService.getAllCourses() } returns courses

      val uniqueCourseNames = courses.map { it.name }.distinct()
      every { courseService.getCourseNames(null) } returns uniqueCourseNames

      mockMvc.get("/courses/course-names") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          uniqueCourseNames.forEachIndexed { index, courseName ->
            jsonPath("$[$index]") { value(courseName) }
          }
        }
      }
    }

    @Test
    fun `getCourseById with correct UUID returns 200 with correct body`() {
      val prerequisites = mutableSetOf(
        PrerequisiteEntityFactory().withName("Prerequisite1").produce(),
        PrerequisiteEntityFactory().withName("Prerequisite2").produce(),
      )

      val expectedCourse = CourseEntityFactory()
        .withPrerequisites(prerequisites)
        .produce()

      every { courseService.getNotWithdrawnCourseById(expectedCourse.id!!) } returns expectedCourse

      mockMvc.get("/courses/${expectedCourse.id}") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          prerequisites.forEachIndexed { index, prerequisite ->
            jsonPath("$.coursePrerequisites[$index].name") { value(prerequisite.name) }
            jsonPath("$.coursePrerequisites[$index].description") { value(prerequisite.description) }
          }
        }
      }

      verify { courseService.getNotWithdrawnCourseById(expectedCourse.id!!) }
    }

    @Test
    fun `getCourseById with random UUID returns 404 with error body`() {
      val randomId = UUID.randomUUID()

      every { courseService.getNotWithdrawnCourseById(any()) } returns null

      mockMvc.get("/courses/$randomId") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isNotFound() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.status") { value(404) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { value("Not Found: No Course found at /courses/$randomId") }
          jsonPath("$.developerMessage") { value("No Course found at /courses/$randomId") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }
    }

    @Test
    fun `getCourseById with invalid UUID returns 400 with error body`() {
      val badId = "bad-id"

      mockMvc.get("/courses/$badId") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isBadRequest() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.status") { value(400) }
          jsonPath("$.errorCode") { isEmpty() }
          jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: $badId") }
          jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: $badId") }
          jsonPath("$.moreInfo") { isEmpty() }
        }
      }

      confirmVerified(courseService)
    }

    @Test
    fun `getCourseById without JWT returns 401`() {
      mockMvc.get("/courses/${UUID.randomUUID()}") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class GetOfferingsTests {
    @Test
    fun `getAllOfferingsByCourseId with JWT returns 200 with correct body`() {
      val offerings = listOf(
        OfferingEntityFactory().withOrganisationId("OF1").withContactEmail("of1@digital.justice.gov.uk").produce(),
        OfferingEntityFactory().withOrganisationId("OF2").withContactEmail("of2@digital.justice.gov.uk").produce(),
        OfferingEntityFactory().withOrganisationId("OF3").withContactEmail("of3@digital.justice.gov.uk").produce(),
      )

      every { courseService.getAllOfferingsByCourseId(any()) } returns offerings

      mockMvc.get("/courses/${UUID.randomUUID()}/offerings") {
        accept = MediaType.APPLICATION_JSON
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          offerings.forEachIndexed { index, offering ->
            jsonPath("$[$index].organisationId") { value(offering.organisationId) }
            jsonPath("$[$index].contactEmail") { value(offering.contactEmail) }
          }
        }
      }
    }

    @Test
    fun `getAllOfferingsByCourseId without JWT returns 401`() {
      mockMvc.get("/courses/${UUID.randomUUID()}/offerings") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }
  }

  @Nested
  inner class PutCoursesTests {
    @Test
    fun `updateCourses with valid CSV data returns 204`() {
      every { courseService.updateCourses(any()) } just Runs

      val replacementCourses = generateCourseRecords(3)
      val replacementCoursesCsv = replacementCourses.toCourseCsv()
      val replacementCoursesDomain = replacementCourses.map { it.toDomain() }

      mockMvc.put("/courses/csv") {
        contentType = MEDIA_TYPE_TEXT_CSV
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = replacementCoursesCsv
      }.andExpect {
        status { isNoContent() }
      }

      verify { courseService.updateCourses(replacementCoursesDomain) }
    }
  }

  @Nested
  inner class PutPrerequisitesTests {
    @Test
    fun `updatePrerequisites with valid CSV data returns 200 and no content`() {
      every { courseService.updatePrerequisites(any()) } returns emptyList()

      val replacementPrerequisites = generatePrerequisiteRecords(3)
      val replacementPrerequisitesCsv = replacementPrerequisites.toPrerequisiteCsv()
      val replacementPrerequisitesDomain = replacementPrerequisites.map { it.toDomain() }

      mockMvc.put("/courses/prerequisites/csv") {
        contentType = MEDIA_TYPE_TEXT_CSV
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = replacementPrerequisitesCsv
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.size()") { value(0) }
        }
      }

      verify { courseService.updatePrerequisites(replacementPrerequisitesDomain) }
    }
  }

  @Nested
  inner class PutOfferingsTests {
    @Test
    fun `updateOfferings with valid CSV data returns 200 and no content`() {
      every { courseService.updateOfferings(any()) } returns emptyList()

      val replacementOfferings = generateOfferingRecords(3)
      val replacementOfferingsCsv = replacementOfferings.toOfferingCsv()
      val replacementOfferingsDomain = replacementOfferings.map { it.toDomain() }

      mockMvc.put("/offerings/csv") {
        contentType = MEDIA_TYPE_TEXT_CSV
        header(AUTHORIZATION, jwtAuthHelper.bearerToken())
        content = replacementOfferingsCsv
      }.andExpect {
        status { isOk() }
        content {
          contentType(MediaType.APPLICATION_JSON)
          jsonPath("$.size()") { value(0) }
        }
      }

      verify { courseService.updateOfferings(replacementOfferingsDomain) }
    }
  }
}
