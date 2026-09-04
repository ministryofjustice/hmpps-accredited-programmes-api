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
      assertThat(source).isEqualTo("Source")
    }
  }

  /**
   * Guards against regressions in the SAR mapping of
   * `CourseParticipationEntity.source` when the value was auto-populated
   * from `referral.referrer.username` and the referrer is not a POM.
   *
   * Seeds a `referrer_user` row for the username but no `staff` row for
   * it, then asserts that the mapper resolves `source` to `null` so the
   * report renders `No Data Held` rather than the raw username.
   */
  @Test
  fun `source is nulled out when it is a referrer username with no matching staff row`() {
    val prisonNumber = "A1234BC"
    val courseId = UUID.randomUUID()
    val offeringId = UUID.randomUUID()
    val referralId = UUID.randomUUID()
    val participationId = UUID.randomUUID()
    val referrerUsername = "ABC123"

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
    // Deliberately no matching `staff` row for this username: referrers
    // are seldom POMs in production.
    persistenceHelper.createReferrerUser(referrerUsername)
    persistenceHelper.createReferral(
      referralId = referralId,
      offeringId = offeringId,
      prisonNumber = prisonNumber,
      referrerUsername = referrerUsername,
      additionalInformation = "Some info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      status = "REFERRAL_STARTED",
      submittedOn = LocalDateTime.now(),
      hasLdc = true,
      hasLdcBeenOverriddenByProgrammeTeam = true,
    )
    // Mirrors the auto-populated write in CourseParticipationService.
    persistenceHelper.createCourseParticipation(
      participationId = participationId,
      referralId = referralId,
      prisonNumber = prisonNumber,
      courseName = "Course 1",
      source = referrerUsername,
      detail = "Detail",
      location = "Location",
      type = CourseSetting.CUSTODY.name,
      outcomeStatus = "INCOMPLETE",
      yearStarted = 2023,
      yearCompleted = 2024,
      createdByUsername = referrerUsername,
      createdDateTime = LocalDateTime.now(),
      lastModifiedByUsername = referrerUsername,
      lastModifiedDateTime = LocalDateTime.now(),
      otherCourseName = "Other course",
      outcomeDetail = "No information to evidence",
    )

    val result = subjectAccessRequestService.getPrisonContentFor(
      prisonNumber,
      LocalDate.now(),
      LocalDate.now().plusDays(1),
    )

    assertNotNull(result)
    val content = result!!.content as SubjectAccessRequestService.Content
    assertThat(content.courseParticipation).hasSize(1)

    with(content.courseParticipation[0]) {
      // Raw referrer username must not surface on the report; the
      // `referrer_user` prefetch nulls it out and the mustache template's
      // `optionalValue` helper renders `No Data Held` instead.
      assertThat(source).isNull()

      // The other username-typed fields on the same row also miss the
      // staff lookup and null-out (their mappers have no free-text
      // fallback).
      assertThat(createdByUser).isNull()
      assertThat(updatedByUser).isNull()
    }
  }
}
