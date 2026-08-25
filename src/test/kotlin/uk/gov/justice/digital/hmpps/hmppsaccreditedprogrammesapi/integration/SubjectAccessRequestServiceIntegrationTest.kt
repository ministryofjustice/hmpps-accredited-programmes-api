package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SubjectAccessRequestServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var subjectAccessRequestService: SubjectAccessRequestService

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
}
