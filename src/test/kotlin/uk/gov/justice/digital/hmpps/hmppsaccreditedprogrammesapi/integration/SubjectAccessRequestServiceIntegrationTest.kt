package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SubjectAccessRequestServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var subjectAccessRequestService: SubjectAccessRequestService

  @Autowired
  private lateinit var jwtAuthorisationHelper: JwtAuthorisationHelper

  @Test
  fun `should return all fields in SAR content`() {
    // Given
    val prisonNumber = "A1234BC"
    val courseId = UUID.randomUUID()
    val offeringId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val participationId = UUID.randomUUID()
    val staffId = 12345.toBigInteger()

    persistenceHelper.clearAllTableContent()

    persistenceHelper.createOrganisation(code = "MDI", name = "HMP Moorland")
    persistenceHelper.createCourse(
      courseId = courseId,
      identifier = "C1",
      name = "Course 1",
      description = "Course 1 Description",
      altName = "C1 Alt Name",
      audience = "General",
      intensity = "HIGH",
      listDisplayName = "Course 1",
    )
    persistenceHelper.createOffering(
      offeringId = offeringId,
      courseId = courseId,
      orgId = "MDI",
      contactEmail = "test@example.com",
      secondaryContactEmail = "test2@example.com",
      referable = true,
    )
    persistenceHelper.createReferrerUser("TEST_USER")
    persistenceHelper.createReferral(
      referralId = referralId,
      offeringId = offeringId,
      prisonNumber = prisonNumber,
      referrerUsername = "TEST_USER",
      additionalInformation = "Some info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      status = "REFERRAL_STARTED",
      submittedOn = LocalDateTime.now(),
      primaryPomStaffId = staffId,
      hasLdc = true,
      hasLdcBeenOverriddenByProgrammeTeam = true,
    )
    persistenceHelper.createCourseParticipation(
      participationId = participationId,
      referralId = referralId,
      prisonNumber = prisonNumber,
      courseName = "Course 1",
      source = "Source",
      detail = "Detail",
      location = "Location",
      type = CourseSetting.CUSTODY.name,
      outcomeStatus = "INCOMPLETE",
      yearStarted = 2023,
      yearCompleted = 2024,
      createdByUsername = "TEST_USER",
      createdDateTime = LocalDateTime.now(),
      lastModifiedByUsername = "TEST_USER",
      lastModifiedDateTime = LocalDateTime.now(),
      otherCourseName = "Other course",
      outcomeDetail = "No information to evidence",
    )
    persistenceHelper.createStaff(
      staffId = staffId,
      firstName = "John",
      lastName = "Doe",
      username = "TEST_USER",
      primaryEmail = "john.doe@test.com",
    )
    // When
    val result = subjectAccessRequestService.getPrisonContentFor(prisonNumber, LocalDate.now(), LocalDate.now().plusDays(1))

    // Then
    assertNotNull(result)
    val content = result!!.content as SubjectAccessRequestService.Content
    assertThat(content.referrals).hasSize(1)
    assertThat(content.courseParticipation).hasSize(1)

    with(content.referrals[0]) {
      assertThat(referrerUsername).isEqualTo("Doe")
      assertThat(primaryPomStaffSurname).isEqualTo("Doe")
      assertThat(secondaryPomStaffSurname).isNull()
      assertThat(hasLdc).isTrue()
      assertThat(hasLdcBeenOverriddenByProgrammeTeam).isTrue()
      assertThat(organisationName).isEqualTo("HMP Moorland")
      assertThat(courseName).isEqualTo("Course 1")
    }

    with(content.courseParticipation[0]) {
      assertThat(courseName).isEqualTo("Course 1")
      assertThat(outcomeStatus).isEqualTo("INCOMPLETE")
      assertThat(otherCourseName).isEqualTo("Other course")
      assertThat(outcomeDetail).isEqualTo("No information to evidence")
    }
  }

  @Test
  fun `should return null when no data is held for the prisoner`() {
    // Given: a prisoner with no seeded referrals and no course participations.
    // No `persistenceHelper.createReferral` / `createCourseParticipation` calls
    // are made for this PRN, so both queries return empty lists.
    val prisonNumber = "Z9999ZZ"
    persistenceHelper.clearAllTableContent()

    // When
    val content = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)

    // Then: per the HMPPS SAR component API spec, a recognised identifier
    // with no held data must produce a 204 response at the HTTP layer. That
    // is driven by the service returning `null`, so we assert that contract
    // here at the service boundary.
    assertThat(content).isNull()
  }

  @Test
  fun `should return HTTP 204 from GET subject-access-request when no data is held for the PRN`() {
    // Given: no seeded data for this prisoner. The service short-circuits
    // to `null` and the hmpps-kotlin starter's SAR controller converts that
    // to HTTP 204. This test locks the contract end-to-end at the HTTP
    // layer -- symmetric with the sibling Community API PR -- so that a
    // future starter-library change to the null -> 204 convention would
    // fail here rather than silently regress in production.
    val prisonNumber = "Z9999ZZ"
    persistenceHelper.clearAllTableContent()

    // Issue a token with ROLE_SAR_DATA_ACCESS (plus this service's own
    // ROLE_ACCREDITED_PROGRAMMES_API) so the request is authorised. Token
    // creation is delegated to the framework-provided helper to reuse the
    // application's configured signing key pair.
    val token = jwtAuthorisationHelper.createJwtAccessToken(
      username = "sar-test",
      clientId = "sar-client",
      roles = listOf("ROLE_SAR_DATA_ACCESS", "ROLE_ACCREDITED_PROGRAMMES_API"),
      scope = emptyList(),
      expiryTime = Duration.ofHours(1),
    )

    // When / Then
    webTestClient
      .get()
      .uri("/subject-access-request?prn=$prisonNumber")
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
      .expectBody().isEmpty
  }
}
