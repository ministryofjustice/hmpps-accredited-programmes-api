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
  fun `should return all courses for a given organisation code including withdrawn courses`() {
    // Given
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    val course1Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course1Uuid, "SC1", "Course Numero Uno", "Sample description", "SC++", "General offence", withdrawn = false)
    persistenceHelper.createOffering(UUID.randomUUID(), course1Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)

    val course2Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course2Uuid, "SC2", "Course Numero Dos", "Sample description", "SC++", "General offence", withdrawn = true)
    persistenceHelper.createOffering(UUID.randomUUID(), course2Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", false)

    val course3Uuid = UUID.randomUUID()
    persistenceHelper.createCourse(course3Uuid, "SC3", "Course Numero Tres", "Sample description", "SC++", "General offence", withdrawn = true)
    persistenceHelper.createOffering(UUID.randomUUID(), course3Uuid, "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", false)

    // When
    val offerings = getAllCoursesForOrganisation("BWN")

    // Then
    assertThat(offerings.size).isEqualTo(3)
    assertThat(offerings.count { it.withdrawn }).isEqualTo(2)
  }

  fun getAllCoursesForOrganisation(organisationId: String): List<CourseEntity> =
    webTestClient
      .get()
      .uri("/organisations/$organisationId/courses")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseEntity>>()
      .returnResult().responseBody!!
}
