package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper
import java.util.UUID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course as ApiCourse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CoursesIntegrationTest
@Autowired constructor(
  val webTestClient: WebTestClient,
  val jwtAuthHelper: JwtAuthHelper,
) {
  companion object {
    const val courseId = "d3abc217-75ee-46e9-a010-368f30282367"
    const val courseOfferingId = "7fffcc6a-11f8-4713-be35-cf5ff1aee517"
  }

  @Test
  fun `get all courses`() {
    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
        [
          { "name": "Lime Course", "alternateName": "LC", "referable": true },
          { "name": "Azure Course", "alternateName": "AC++", "referable": true },
          { "name": "Violet Course", "referable": true }
        ]
        """,
      )
  }

  @Test
  fun `get a course - happy path`() {
    webTestClient
      .get()
      .uri("/courses/$courseId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseId)
  }

  @Test
  fun `get a course - not found`() {
    val randomId = UUID.randomUUID()

    webTestClient
      .get()
      .uri("/courses/$randomId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.status").isEqualTo(404)
      .jsonPath("$.errorCode").isEmpty
      .jsonPath("$.userMessage").value(startsWith("Not Found: No Course found at /courses/$randomId"))
      .jsonPath("$.developerMessage").value(startsWith("No Course found at /courses/$randomId"))
      .jsonPath("$.moreInfo").isEmpty
  }

  @Test
  fun `get a course by offering id`() {
    webTestClient
      .get()
      .uri("/offerings/$courseOfferingId/course")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseId)
  }

  @Test
  fun `get all offerings for a course`() {
    webTestClient
      .get()
      .uri("/courses/$courseId/offerings")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .json(
        """
        [
          { "organisationId": "MDI", "contactEmail":"nobody-mdi@digital.justice.gov.uk" },
          { "organisationId": "BWN", "contactEmail":"nobody-bwn@digital.justice.gov.uk" },
          { "organisationId": "BXI", "contactEmail":"nobody-bxi@digital.justice.gov.uk" }
        ]
      """,
      )
  }

  @Test
  fun `get a course offering using short url - happy path`() {
    webTestClient
      .get()
      .uri("/offerings/$courseOfferingId")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(courseOfferingId)
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
  }

  @DirtiesContext
  @Test
  fun `put courses csv`() {
    uploadCourses(CsvTestData.emptyCoursesCsvText)
    allCourses() shouldHaveSize 0

    uploadCourses(CsvTestData.coursesCsvText)
    val originalCourses = allCourses()
    originalCourses shouldHaveSize CsvTestData.newCourses.size

    uploadCourses(CsvTestData.emptyCoursesCsvText)
    allCourses() shouldHaveSize 0

    uploadCourses(CsvTestData.coursesCsvText)
    val restoredCourses = allCourses()
    restoredCourses shouldHaveSize CsvTestData.newCourses.size
    originalCourses.map { it.id } shouldContainExactlyInAnyOrder restoredCourses.map { it.id }

    uploadCourses(CsvTestData.courseUpdatesCsvText)
    val updatedCourses: List<Course> = allCourses()
    updatedCourses shouldHaveSize CsvTestData.newCourseUpdates.size
    (originalCourses.map { it.id } - updatedCourses.map { it.id }) shouldHaveSize 1
  }

  @DirtiesContext
  @Test
  fun `round-trip courses csv`() {
    uploadCourses(CsvTestData.coursesCsvText)
    val originalCourses = allCourses()
    originalCourses shouldHaveSize CsvTestData.newCourses.size

    val coursesCsv = allCoursesCsv()

    uploadCourses(CsvTestData.emptyCoursesCsvText)
    allCourses() shouldHaveSize 0

    uploadCourses(coursesCsv)

    val roundTripCourses = allCourses()
    roundTripCourses shouldHaveSize CsvTestData.newCourses.size
    originalCourses.map { it.id } shouldContainExactlyInAnyOrder roundTripCourses.map { it.id }
    originalCourses shouldContainExactlyInAnyOrder roundTripCourses
  }

  @DirtiesContext
  @Test
  fun `put prerequisites csv`() {
    uploadCourses(CsvTestData.coursesCsvText)

    uploadPrerequisites(CsvTestData.prerequisitesCsvText)

    webTestClient
      .get()
      .uri("/courses")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody()
      .jsonPath("$..coursePrerequisites.length()").isEqualTo(35)
  }

  @DirtiesContext
  @Test
  fun `Round-trip prerequisites csv`() {
    uploadCourses(CsvTestData.coursesCsvText)
    uploadPrerequisites(CsvTestData.prerequisitesCsvText)

    val originalCourses = allCourses()
    val prerequisitesCsv = allPrerequisitesCsv()

    uploadPrerequisites(CsvTestData.emmptyPrerequisitesCsvText)
    allCourses().flatMap { it.coursePrerequisites } shouldHaveSize 0

    uploadPrerequisites(prerequisitesCsv)

    originalCourses shouldContainExactlyInAnyOrder allCourses()
  }

  @DirtiesContext
  @Test
  fun `put offerings csv`() {
    uploadCourses(CsvTestData.coursesCsvText)

    uploadOfferings(CsvTestData.offeringsCsvText)

    val courses: List<ApiCourse> = allCourses()

    val allOfferings: List<List<CourseOffering>> = getAllOfferings(courses)

    allOfferings shouldHaveSize CsvTestData.newCourses.size

    val actualOrganisationIds: Set<String> = allOfferings
      .flatMap { courseOfferings ->
        courseOfferings.map { offering -> offering.organisationId }
      }.toSet()

    val expectedOrganisationIds = CsvTestData.offeringsRecords.map { it.prisonId }.toSet()

    actualOrganisationIds shouldContainExactly expectedOrganisationIds

    uploadOfferings(CsvTestData.emptyOfferingsCsvText)

    getAllOfferings(courses).allOfferingIds() shouldHaveSize 0

    uploadOfferings(CsvTestData.offeringsCsvText)

    getAllOfferings(courses).allOfferingIds() shouldContainExactly allOfferings.allOfferingIds()
  }

  @DirtiesContext
  @Test
  fun `Round-trip offerings csv`() {
    uploadCourses(CsvTestData.coursesCsvText)
    uploadOfferings(CsvTestData.offeringsCsvText)

    val courses: List<ApiCourse> = allCourses()

    val allOfferings: List<List<CourseOffering>> = getAllOfferings(courses)

    val offeringsCsv = allOfferingsCsv()
    uploadOfferings(CsvTestData.emptyOfferingsCsvText)
    uploadOfferings(offeringsCsv)

    getAllOfferings(courses).allOfferingIds() shouldContainExactly allOfferings.allOfferingIds()
    getAllOfferings(courses).flatten() shouldContainExactly allOfferings.flatten()
  }

  private fun uploadCourses(csvData: String) {
    webTestClient
      .put()
      .uri("/courses/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(csvData)
      .exchange()
      .expectStatus().is2xxSuccessful
  }

  private fun uploadOfferings(csvContent: String) {
    webTestClient
      .put()
      .uri("/offerings/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(csvContent)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  private fun uploadPrerequisites(csvContent: String) {
    webTestClient
      .put()
      .uri("/courses/prerequisites/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(csvContent)
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .jsonPath("$.size()").isEqualTo(0)
  }

  private fun getAllOfferings(courses: List<Course>) =
    courses.map {
      webTestClient
        .get()
        .uri("/courses/${it.id}/offerings")
        .headers(jwtAuthHelper.authorizationHeaderConfigurer())
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectBody(object : ParameterizedTypeReference<List<CourseOffering>>() {})
        .returnResult().responseBody!!
    }

  private fun allCourses() = webTestClient
    .get()
    .uri("/courses")
    .headers(jwtAuthHelper.authorizationHeaderConfigurer())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBodyList(ApiCourse::class.java)
    .returnResult().responseBody!!

  private fun allCoursesCsv(): String {
    return webTestClient
      .get()
      .uri("/courses/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult().responseBody!!
  }

  private fun allOfferingsCsv(): String =
    webTestClient
      .get()
      .uri("/offerings/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult().responseBody!!

  private fun allPrerequisitesCsv(): String =
    webTestClient
      .get()
      .uri("/courses/prerequisites/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult().responseBody!!
}

private val MEDIA_TYPE_TEXT_CSV: MediaType = MediaType("text", "csv")
private fun List<List<CourseOffering>>.allOfferingIds() = flatMap { it.map(CourseOffering::id) }.toSet()
