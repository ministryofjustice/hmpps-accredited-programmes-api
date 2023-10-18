package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.TEST_USER_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class CourseParticipationIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Creating a course participation should return 201 with correct body`() {
    val startTime = LocalDateTime.now()

    val created = createCourseParticipation(
      CourseParticipationCreate(
        courseName = "Course name",
        prisonNumber = "A1234AA",
        source = "Source of information",
        detail = "Course detail",
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
          location = "location",
        ),
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.complete,
          yearStarted = 2021,
          yearCompleted = 2022,
        ),
      ),
    )

    created.shouldNotBeNull()
    created.id.shouldNotBeNull()

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = created.courseName,
        prisonNumber = created.prisonNumber,
        source = created.source,
        detail = created.detail,
        setting = CourseParticipationSetting(
          type = created.setting!!.type,
          location = created.setting?.location,
        ),
        outcome = CourseParticipationOutcome(
          status = created.outcome!!.status,
          yearStarted = created.outcome?.yearStarted,
          yearCompleted = created.outcome?.yearCompleted,
        ),
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )

    LocalDateTime.parse(retrieved.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Creating a course participation with minimal fields should tolerantly return 201 with correct body`() {
    val created = createCourseParticipation(
      CourseParticipationCreate(
        courseName = null,
        prisonNumber = randomPrisonNumber(),
        setting = null,
        outcome = null,
      ),
    )

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = null,
        prisonNumber = created.prisonNumber,
        source = null,
        detail = null,
        setting = null,
        outcome = null,
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )
  }

  @Test
  fun `Updating a course participation should return 200 with correct body`() {
    val startTime = LocalDateTime.now()

    val created = createCourseParticipation(
      CourseParticipationCreate(
        prisonNumber = "A1234AA",
      ),
    )

    val updated = updateCourseParticipation(
      created.id,
      CourseParticipationUpdate(
        courseName = "Course name",
        setting = CourseParticipationSetting(
          type = CourseParticipationSettingType.custody,
        ),
        source = "Source of information",
        detail = "Course participation detail",
        outcome = CourseParticipationOutcome(
          status = CourseParticipationOutcome.Status.incomplete,
          yearStarted = 2020,
        ),
      ),
    )!!

    val retrieved = getCourseParticipation(created.id)

    retrieved.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = created.id,
        courseName = updated.courseName,
        setting = CourseParticipationSetting(
          type = updated.setting!!.type,
        ),
        prisonNumber = created.prisonNumber,
        source = updated.source,
        detail = updated.detail,
        outcome = CourseParticipationOutcome(
          status = updated.outcome!!.status,
          yearStarted = updated.outcome?.yearStarted,
        ),
        addedBy = TEST_USER_NAME,
        createdAt = LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME),
      ),
      CourseParticipation::createdAt,
    )

    updated.shouldBeEqualToIgnoringFields(retrieved, CourseParticipation::createdAt)
    LocalDateTime.parse(updated.createdAt) shouldBeGreaterThanOrEqualTo startTime
  }

  @Test
  fun `Finding course participations by prison number should return 200 with matching entries`() {
    val expectedPrisonNumber = randomPrisonNumber()
    val otherPrisonNumber = randomPrisonNumber()

    val expectedPrisonNumberCourseIds = getCourseIds().map {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = expectedPrisonNumber,
        ),
      ).id
    }.toSet()

    expectedPrisonNumberCourseIds.shouldNotBeEmpty()

    val randomPrisonNumber = getCourseIds().map {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = otherPrisonNumber,
        ),
      ).id
    }.toSet()

    randomPrisonNumber.intersect(expectedPrisonNumberCourseIds).shouldBeEmpty()

    val retrieved = getCourseParticipationsForPrisonNumber(expectedPrisonNumber).map { it.id }.toSet()
    retrieved shouldBe expectedPrisonNumberCourseIds
  }

  @Test
  fun `Finding course participations by random prison number should return 200 with no matching entries`() {
    getCourseIds().forEach {
      createCourseParticipation(
        CourseParticipationCreate(
          prisonNumber = randomPrisonNumber(),
        ),
      )
    }

    getCourseParticipationsForPrisonNumber("Z0000ZZ").shouldBeEmpty()
  }

  @Test
  fun `Deleting a course participation by id should return 204 with no body`() {
    val created = createCourseParticipation(
      CourseParticipationCreate(
        prisonNumber = "X9999XX",
      ),
    )

    getCourseParticipationStatusCode(created.id) shouldBe HttpStatus.OK
    deleteCourseParticipation(created.id)

    getCourseParticipationStatusCode(created.id) shouldBe HttpStatus.NOT_FOUND
    deleteCourseParticipation(created.id)
  }

  private fun createCourseParticipation(courseParticipationToAdd: CourseParticipationCreate): CourseParticipation =
    webTestClient
      .post()
      .uri("/course-participations")
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(courseParticipationToAdd)
      .exchange()
      .expectStatus().isCreated
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun updateCourseParticipation(id: UUID, update: CourseParticipationUpdate): CourseParticipation? =
    webTestClient
      .put()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(update)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody

  private fun deleteCourseParticipation(id: UUID) =
    webTestClient
      .delete()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent

  private fun getCourseParticipation(id: UUID): CourseParticipation =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<CourseParticipation>()
      .returnResult().responseBody!!

  private fun getCourseParticipationStatusCode(id: UUID): HttpStatusCode =
    webTestClient
      .get()
      .uri("/course-participations/{id}", id)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .returnResult<Any>().status

  private fun getCourseParticipationsForPrisonNumber(prisonNumber: String): List<CourseParticipation> =
    webTestClient
      .get()
      .uri("/people/{prisonNumber}/course-participations", prisonNumber)
      .headers(jwtAuthHelper.authorizationHeaderConfigurer())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseParticipation>>()
      .returnResult().responseBody!!
}
