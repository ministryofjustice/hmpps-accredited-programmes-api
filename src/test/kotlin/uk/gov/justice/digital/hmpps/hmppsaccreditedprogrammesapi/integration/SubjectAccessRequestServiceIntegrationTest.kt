package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
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
    )
    persistenceHelper.createAuditRecord(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.CREATE_REFERRAL.name,
      auditUsername = "TEST_USER",
      referrerUsername = "TEST_USER",
    )
    persistenceHelper.createPniResult(
      prisonNumber = prisonNumber,
      pniResultJson = "{\"result\": \"success\"}",
    )
    persistenceHelper.createReferralStatusHistory(
      referralId = referralId,
      username = "TEST_USER",
      status = "REFERRAL_STARTED",
    )
    persistenceHelper.createStaff(
      staffId = 12345.toBigInteger(),
      firstName = "John",
      lastName = "Doe",
      username = "JDOE",
      primaryEmail = "john.doe@example.com",
    )

    // When
    val result = subjectAccessRequestService.getPrisonContentFor(prisonNumber, LocalDate.now(), LocalDate.now().plusDays(1))

    // Then
    assertNotNull(result)
    val content = result!!.content as SubjectAccessRequestService.Content
    assertThat(content.referrals).hasSize(1)
    assertThat(content.courseParticipation).hasSize(1)
    assertThat(content.auditRecords).hasSize(1)
    assertThat(content.courses).hasSize(1)
    assertThat(content.pniResults).hasSize(1)
    assertThat(content.referralStatusHistory).hasSize(1)
    assertThat(content.staff).hasSize(1)

    with(content.referrals[0]) {
      assertThat(prisonerNumber).isEqualTo(prisonNumber)
      assertThat(courseName).isEqualTo("Course 1")
      assertThat(referrerUsername).isEqualTo("TEST_USER")
    }

    with(content.courseParticipation[0]) {
      assertThat(prisonNumber).isEqualTo(prisonNumber)
      assertThat(courseName).isEqualTo("Course 1")
      assertThat(outcomeStatus).isEqualTo("INCOMPLETE")
    }

    with(content.auditRecords[0]) {
      assertThat(auditUsername).isEqualTo("TEST_USER")
      assertThat(referrerUsername).isEqualTo("TEST_USER")
    }

    with(content.courses[0]) {
      assertThat(description).isEqualTo("Course 1 Description")
      assertThat(listDisplayName).isEqualTo("Course 1")
    }

    with(content.pniResults[0]) {
      assertThat(pniResultJson).isEqualTo("{\"result\": \"success\"}")
    }

    with(content.staff[0]) {
      assertThat(firstName).isEqualTo("John")
      assertThat(lastName).isEqualTo("Doe")
      assertThat(username).isEqualTo("JDOE")
    }
  }
}
