package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Prerequisite
import java.util.UUID

@WebMvcTest
@ContextConfiguration(classes = [CoursesControllerTest::class])
@ComponentScan(
  basePackages = [
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi",
    "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api",
  ],
)
@Import(JwtAuthHelper::class)
class CoursesControllerTest(
  @Autowired val mockMvc: MockMvc,
  @Autowired val jwtAuthHelper: JwtAuthHelper,
) {

  @MockkBean
  private lateinit var coursesService: CourseService

  @Nested
  inner class GetCoursesTests {
    @Test
    fun `coursesGet with JWT returns 200 with correct body`() {
      val courses = listOf(
        CourseEntityFactory().withName("Course1").produce(),
        CourseEntityFactory().withName("Course2").produce(),
        CourseEntityFactory().withName("Course3").produce(),
      )

      every { coursesService.allCourses() } returns courses

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
    fun `coursesGet without JWT returns 401`() {
      mockMvc.get("/courses") {
        accept = MediaType.APPLICATION_JSON
      }.andExpect {
        status { isUnauthorized() }
      }
    }

    @Test
    fun `coursesCourseIdGet with correct UUID returns 200 with correct body`() {
      val prerequisites = mutableSetOf(
        Prerequisite(name = "Prerequisite1", description = randomSentence(1..10)),
        Prerequisite(name = "Prerequisite2", description = randomSentence(1..10)),
      )

      val expectedCourse = CourseEntityFactory()
        .withPrerequisites(prerequisites)
        .produce()

      every { coursesService.course(expectedCourse.id!!) } returns expectedCourse

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

      verify { coursesService.course(expectedCourse.id!!) }
    }

    @Test
    fun `coursesCourseIdGet with random UUID returns 404 with error body`() {
      val randomId = UUID.randomUUID()

      every { coursesService.course(any()) } returns null

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
    fun `coursesCourseIdGet with invalid UUID returns 400 with error body`() {
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

      confirmVerified(coursesService)
    }

    @Test
    fun `coursesCourseIdGet without JWT returns 401`() {
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
    fun `coursesCourseIdOfferingsGet with JWT returns 200 with correct body`() {
      val offerings = listOf(
        OfferingEntityFactory().withOrganisationId("OF1").withContactEmail("of1@digital.justice.gov.uk").produce(),
        OfferingEntityFactory().withOrganisationId("OF2").withContactEmail("of2@digital.justice.gov.uk").produce(),
        OfferingEntityFactory().withOrganisationId("OF3").withContactEmail("of3@digital.justice.gov.uk").produce(),
      )

      every { coursesService.offeringsForCourse(any()) } returns offerings

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
    fun `coursesCourseIdOfferingsGet without JWT returns 401`() {
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
    fun `coursesPut with valid CSV data returns 204`() {
      every { coursesService.updateCourses(any()) } just Runs

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

      verify { coursesService.updateCourses(replacementCoursesDomain) }
    }
  }

  @Nested
  inner class PutPrerequisitesTests {
    @Test
    fun `coursesPrerequisitesPut with valid CSV data returns 200 and no content`() {
      every { coursesService.replaceAllPrerequisites(any()) } returns emptyList()

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

      verify { coursesService.replaceAllPrerequisites(replacementPrerequisitesDomain) }
    }
  }

  @Nested
  inner class PutOfferingsTests {
    @Test
    fun `coursesOfferingsPut with valid CSV data returns 200 and no content`() {
      every { coursesService.updateOfferings(any()) } returns emptyList()

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

      verify { coursesService.updateOfferings(replacementOfferingsDomain) }
    }
  }
}
