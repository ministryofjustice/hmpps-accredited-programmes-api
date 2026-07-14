package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
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
    val sexualOffenceId = UUID.randomUUID()
    val expectedReferralId = referralId
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
    persistenceHelper.createAuditRecord(
      prisonNumber = prisonNumber,
      auditAction = AuditAction.CREATE_REFERRAL.name,
      auditUsername = "TEST_USER",
      referrerUsername = "TEST_USER",
    )
    persistenceHelper.createPniResult(
      prisonNumber = prisonNumber,
      pniResultJson = "{\"result\": \"success\"}",
      crn = "X1234YZ",
      programmePathway = "ALTERNATIVE_PATHWAY",
    )
    persistenceHelper.createOasysPniResult(
      prisonNumber = prisonNumber,
      oasysAssessmentId = 1234,
      programmePathway = "HIGH_INTENSITY_BC",
    )
    persistenceHelper.createPerson(
      prisonNumber = prisonNumber,
      forename = "John",
      surname = "Doe",
      earliestReleaseDateType = "CRD",
      sentenceType = "Determinate",
      location = "HMP Moorland",
      gender = "Male",
    )
    persistenceHelper.createSexualOffenceDetails(
      sexualOffenceDetailsEntity = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity(
        id = sexualOffenceId,
        category = SexualOffenceCategoryType.AGAINST_MINORS,
        description = "Example sexual offence",
        hintText = "hint",
        score = 2,
      ),
    )
    persistenceHelper.createSelectedSexualOffenceDetails(
      referralId = referralId,
      sexualOffenceDetailsId = sexualOffenceId,
    )
    persistenceHelper.createReferralStatusHistory(
      referralId = referralId,
      username = "TEST_USER",
      status = "REFERRAL_STARTED",
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
    assertThat(content.person).isNotNull
    assertThat(content.oasysPniResults).hasSize(1)
    assertThat(content.referralStatusHistory).hasSize(1)
    assertThat(content.selectedSexualOffenceDetails).hasSize(1)
    assertThat(content.sexualOffenceDetails).hasSize(1)

    with(content.referrals[0]) {
      assertThat(prisonerNumber).isEqualTo(prisonNumber)
      assertThat(referrerUsername).isEqualTo("TEST_USER")
      assertThat(hasLdc).isTrue()
      assertThat(hasLdcBeenOverriddenByProgrammeTeam).isTrue()
    }

    with(content.courseParticipation[0]) {
      assertThat(prisonNumber).isEqualTo(prisonNumber)
      assertThat(courseName).isEqualTo("Course 1")
      assertThat(outcomeStatus).isEqualTo("INCOMPLETE")
      assertThat(otherCourseName).isEqualTo("Other course")
      assertThat(outcomeDetail).isEqualTo("No information to evidence")
    }

    with(content.auditRecords[0]) {
      assertThat(auditUsername).isEqualTo("TEST_USER")
      assertThat(referrerUsername).isEqualTo("TEST_USER")
      assertThat(prisonNumber).isEqualTo("A1234BC")
    }

    with(content.courses[0]) {
      assertThat(name).isEqualTo("Course 1")
    }

    with(content.pniResults[0]) {
      assertThat(crn).isEqualTo("X1234YZ")
      assertThat(pniResultJson).isEqualTo("{\"result\": \"success\"}")
    }

    with(content.person!!) {
      assertThat(forename).isEqualTo("John")
      assertThat(surname).isEqualTo("Doe")
      assertThat(location).isEqualTo("HMP Moorland")
    }

    with(content.oasysPniResults[0]) {
      assertThat(prisonNumber).isEqualTo("A1234BC")
      assertThat(programmePathway).isEqualTo("HIGH_INTENSITY_BC")
    }

    with(content.selectedSexualOffenceDetails[0]) {
      assertThat(referralId).isEqualTo(expectedReferralId)
      assertThat(sexualOffenceDetailsId).isEqualTo(sexualOffenceId)
    }

    with(content.sexualOffenceDetails[0]) {
      assertThat(id).isEqualTo(sexualOffenceId)
      assertThat(score).isEqualTo(2)
    }
  }
}
