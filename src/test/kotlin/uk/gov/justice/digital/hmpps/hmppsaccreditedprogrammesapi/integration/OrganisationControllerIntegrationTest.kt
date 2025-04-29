package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Organisation
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OrganisationControllerIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()
  }

  @Test
  fun `should return all courses for a given organisation code which are not withdrawn`() {
    // Given
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    val course1Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course1Uuid, "SC1", "Course Numero Uno", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.randomUUID(), course1Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, false)

    val course2Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course2Uuid, "SC2", "Course Numero Dos", "Sample description", "SC++", "General offence")
    val offeringId2 = UUID.randomUUID()
    persistenceHelper.createOffering(offeringId2, course2Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, false)

    val course3Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course3Uuid, "SC3", "Course Numero Tres", "Sample description", "SC++", "General offence")
    val offeringId3 = UUID.randomUUID()
    persistenceHelper.createOffering(offeringId3, course3Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, true)

    // When
    val courses = getAllCoursesForOrganisation("BWN")

    // Then
    assertThat(courses.size).isEqualTo(2)
    assertThat(courses).extracting("identifier").containsExactlyInAnyOrder("SC1", "SC2")
    assertThat(courses).extracting("name").containsExactlyInAnyOrder("Course Numero Uno", "Course Numero Dos")
  }

  @Test
  fun `should only return withdrawn course offerings for a given organisation code for which referrals exist`() {
    // Given
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    val course1Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course1Uuid, "SC1", "Course Numero Uno", "Sample description", "SC++", "General offence", withdrawn = true)
    persistenceHelper.createOffering(UUID.randomUUID(), course1Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, true)

    val course2Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course2Uuid, "SC2", "Course Numero Dos", "Sample description", "SC++", "General offence", withdrawn = true)
    val offeringId2 = UUID.randomUUID()
    persistenceHelper.createOffering(offeringId2, course2Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, true)

    val course3Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course3Uuid, "SC3", "Course Numero Tres", "Sample description", "SC++", "General offence", withdrawn = true)
    val offeringId3 = UUID.randomUUID()
    persistenceHelper.createOffering(offeringId3, course3Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true, true)
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferral(UUID.randomUUID(), offeringId3, "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)

    // When
    val courses = getAllCoursesForOrganisation("BWN")

    // Then
    assertThat(courses.size).isEqualTo(1)
    assertThat(courses[0].identifier).isEqualTo("SC3")
    assertThat(courses[0].name).isEqualTo("Course Numero Tres")
  }

  @Test
  fun `should return organisation details including gender for a known organisation code`() {
    // Given
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org", gender = "MALE")

    // When
    val organisation = getOrganisation("BWN")

    // Then
    assertThat(organisation.code).isEqualTo("BWN")
    assertThat(organisation.gender).isEqualTo("MALE")
  }

  @Test
  fun `should return a 404 with ErrorResponse for an unknown organisation code`() {
    // Given
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org", gender = "MALE")

    // When
    val errorResponse = webTestClient
      .get()
      .uri("/organisation/UNKNOWN_ORGANISATION_CODE")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!

    // Then
    assertThat(errorResponse).isNotNull
    assertThat(errorResponse.status).isEqualTo(404)
    assertThat(errorResponse.userMessage).isEqualTo("Not Found: No Organisation found at /organisation/UNKNOWN_ORGANISATION_CODE")
    assertThat(errorResponse.developerMessage).isEqualTo("No Organisation found at /organisation/UNKNOWN_ORGANISATION_CODE")
  }

  fun getAllCoursesForOrganisation(organisationId: String): List<CourseEntity> = webTestClient
    .get()
    .uri("/organisations/$organisationId/courses")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<List<CourseEntity>>()
    .returnResult().responseBody!!

  fun getOrganisation(organisationCode: String): Organisation = webTestClient
    .get()
    .uri("/organisation/$organisationCode")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<Organisation>()
    .returnResult().responseBody!!
}
