package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.restapi.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseIntegrationTest : IntegrationTestBase() {
  companion object {
    val COURSE_ID: UUID = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")
    val COURSE_OFFERING_ID: UUID = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  }

  @Test
  fun `Searching for all courses with JWT returns 200 with correct body`() {
    val courseRecords = generateCourseRecords(3)
    updateCoursesCsv(courseRecords.toCourseCsv())

    courseRecords.forEach { courseRecord ->
      val matchingCourse = getAllCourses().find { it.name == courseRecord.name }
      matchingCourse.shouldNotBeNull()
    }
  }

  @Test
  fun `Searching for all courses without JWT returns 401`() {
    webTestClient
      .get()
      .uri("/courses")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Searching for a course with JWT and valid id returns 200 with correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID.toString())
  }

  @Test
  fun `Searching for a course with JWT and random id returns 404 with error body`() {
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
  fun `Searching for a course by offering id with JWT returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/offerings/$COURSE_OFFERING_ID/course")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_ID.toString())
  }

  @Test
  fun `Searching for all offerings for a course with JWT and valid id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/courses/$COURSE_ID/offerings")
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
  fun `Searching for all offerings with JWT and correct course offering id returns 200 and correct body`() {
    webTestClient
      .get()
      .uri("/offerings/$COURSE_OFFERING_ID")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.id").isEqualTo(COURSE_OFFERING_ID.toString())
      .jsonPath("$.organisationId").isNotEmpty
      .jsonPath("$.contactEmail").isNotEmpty
  }

  @DirtiesContext
  @Test
  fun `Uploading valid course records in CSV format will process and persist them`() {
    val emptyCourseRecords = generateCourseRecords(0)
    updateCoursesCsv(emptyCourseRecords.toCourseCsv())

    getAllCourses() shouldHaveSize emptyCourseRecords.size

    val originalCourseRecords = generateCourseRecords(3)
    updateCoursesCsv(originalCourseRecords.toCourseCsv())

    val originalCourses = getAllCourses()

    originalCourses shouldHaveSize originalCourseRecords.size

    updateCoursesCsv(emptyCourseRecords.toCourseCsv())

    getAllCourses() shouldHaveSize emptyCourseRecords.size

    updateCoursesCsv(originalCourseRecords.toCourseCsv())

    val restoredCourses = getAllCourses()

    restoredCourses shouldHaveSize originalCourseRecords.size
    originalCourses.map { it.id } shouldContainExactlyInAnyOrder restoredCourses.map { it.id }

    val updatedCourseRecords = generateCourseRecords(4)

    updateCoursesCsv(updatedCourseRecords.toCourseCsv())

    val finalCourses = getAllCourses()

    val commonIds = originalCourses.map { it.id }.toSet() intersect finalCourses.map { it.id }.toSet()
    val newIds = finalCourses.map { it.id }.toSet() subtract originalCourses.map { it.id }.toSet()

    commonIds shouldHaveAtLeastSize 1
    newIds shouldHaveAtLeastSize 1
    finalCourses shouldHaveSize updatedCourseRecords.size
  }

  @DirtiesContext
  @Test
  fun `Valid course records in CSV format maintain their internal consistency between uploads`() {
    val courseRecordsToUpload = generateCourseRecords(3)
    updateCoursesCsv(courseRecordsToUpload.toCourseCsv())

    val uploadedCourses = getAllCourses()

    uploadedCourses.shouldHaveSize(courseRecordsToUpload.size)

    val downloadedCoursesCsv = webTestClient
      .get()
      .uri("/courses/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

    val emptyCourseRecords = generateCourseRecords(0)

    updateCoursesCsv(emptyCourseRecords.toCourseCsv())

    getAllCourses() shouldHaveSize emptyCourseRecords.size

    updateCoursesCsv(downloadedCoursesCsv)

    val reuploadedCourses = getAllCourses()

    reuploadedCourses.shouldHaveSize(courseRecordsToUpload.size)
    uploadedCourses.map { it.id } shouldContainExactlyInAnyOrder reuploadedCourses.map { it.id }
    uploadedCourses shouldContainExactlyInAnyOrder reuploadedCourses
  }

  @DirtiesContext
  @Test
  fun `Uploading valid prerequisite records in CSV format will process and persist them`() {
    val courseRecordsToUpload = generateCourseRecords(3)
    updateCoursesCsv(courseRecordsToUpload.toCourseCsv())

    val prerequisiteRecordsToUpload = generatePrerequisiteRecords(3)
    updatePrerequisitesCsv(prerequisiteRecordsToUpload.toPrerequisiteCsv())

    getAllCourses().flatMap { it.coursePrerequisites } shouldHaveSize prerequisiteRecordsToUpload.size
  }

  @DirtiesContext
  @Test
  fun `Valid prerequisite records in CSV format maintain their internal consistency between uploads`() {
    val courseRecordsToUpload = generateCourseRecords(3)
    updateCoursesCsv(courseRecordsToUpload.toCourseCsv())

    val prerequisiteRecordsToUpload = generatePrerequisiteRecords(3)
    updatePrerequisitesCsv(prerequisiteRecordsToUpload.toPrerequisiteCsv())

    val uploadedCourses = getAllCourses()

    val downloadedPrerequisitesCsv = webTestClient
      .get()
      .uri("/courses/prerequisites/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

    val emptyPrerequisiteRecords = generatePrerequisiteRecords(0)

    updatePrerequisitesCsv(emptyPrerequisiteRecords.toPrerequisiteCsv())

    getAllCourses().flatMap { it.coursePrerequisites } shouldHaveSize emptyPrerequisiteRecords.size

    updatePrerequisitesCsv(downloadedPrerequisitesCsv)

    val coursesWithUpdatedPrerequisites = getAllCourses()

    coursesWithUpdatedPrerequisites shouldContainExactlyInAnyOrder uploadedCourses
  }

  @DirtiesContext
  @Test
  fun `Uploading valid offering records in CSV format will process and persist them`() {
    val courseRecordsToUpload = generateCourseRecords(3)
    updateCoursesCsv(courseRecordsToUpload.toCourseCsv())

    val offeringRecordsToUpload = generateOfferingRecords(3)
    updateOfferingsCsv(offeringRecordsToUpload.toOfferingCsv())

    val uploadedCourses = getAllCourses()

    val uploadedOfferings = uploadedCourses.map { getAllOfferingsForCourse(it.id) }
    val uploadedOfferingsIds = uploadedOfferings.flatten().map { it.id }.toSet()

    uploadedOfferings shouldHaveSize offeringRecordsToUpload.size

    val actualOrganisationIds: Set<String> = uploadedOfferings
      .flatMap { courseOfferings ->
        courseOfferings.map { offering -> offering.organisationId }
      }.toSet()

    val expectedOrganisationIds = offeringRecordsToUpload.map { it.prisonId }.toSet()

    actualOrganisationIds shouldContainExactly expectedOrganisationIds

    val emptyOfferingRecords = generateOfferingRecords(0)
    updateOfferingsCsv(emptyOfferingRecords.toOfferingCsv())

    val emptyOfferings = uploadedCourses.map { getAllOfferingsForCourse(it.id) }
    val emptyOfferingsIds = emptyOfferings.flatten().map { it.id }.toSet()

    emptyOfferingsIds shouldHaveSize emptyOfferingRecords.size

    updateOfferingsCsv(offeringRecordsToUpload.toOfferingCsv())

    val reuploadedOfferings = uploadedCourses.map { getAllOfferingsForCourse(it.id) }
    val reuploadedOfferingsIds = reuploadedOfferings.flatten().map { it.id }.toSet()

    reuploadedOfferingsIds shouldContainExactly uploadedOfferingsIds
  }

  @DirtiesContext
  @Test
  fun `Valid offering records in CSV format maintain their internal consistency between uploads`() {
    val courseRecordsToUpload = generateCourseRecords(3)
    updateCoursesCsv(courseRecordsToUpload.toCourseCsv())

    val offeringRecordsToUpload = generateOfferingRecords(3)
    updateOfferingsCsv(offeringRecordsToUpload.toOfferingCsv())

    val uploadedCourses = getAllCourses()
    val uploadedOfferings = uploadedCourses.map { getAllOfferingsForCourse(it.id) }
    val uploadedOfferingsIds = uploadedOfferings.flatten().map { it.id }.toSet()

    val downloadedOfferingsCsv = webTestClient
      .get()
      .uri("/offerings/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MEDIA_TYPE_TEXT_CSV)
      .exchange()
      .expectStatus().isOk
      .expectBody<String>()
      .returnResult().responseBody!!

    val emptyOfferingRecords = generateOfferingRecords(0)
    updateOfferingsCsv(emptyOfferingRecords.toOfferingCsv())

    updateOfferingsCsv(downloadedOfferingsCsv)

    val reuploadedOfferings = uploadedCourses.map { getAllOfferingsForCourse(it.id) }
    val reuploadedOfferingsIds = reuploadedOfferings.flatten().map { it.id }.toSet()

    reuploadedOfferingsIds shouldContainExactly uploadedOfferingsIds
  }

  fun updateCoursesCsv(coursesCsvData: String): EntityExchangeResult<Void> =
    webTestClient
      .put()
      .uri("/courses/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(coursesCsvData)
      .exchange()
      .expectStatus().isNoContent
      .expectBody().isEmpty

  fun updateOfferingsCsv(offeringsCsvData: String): WebTestClient.ResponseSpec =
    webTestClient
      .put()
      .uri("/offerings/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(offeringsCsvData)
      .exchange()
      .expectStatus().isOk

  fun updatePrerequisitesCsv(prerequisitesCsvData: String): WebTestClient.ResponseSpec =
    webTestClient
      .put()
      .uri("/courses/prerequisites/csv")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MEDIA_TYPE_TEXT_CSV)
      .bodyValue(prerequisitesCsvData)
      .exchange()
      .expectStatus().isOk
}
