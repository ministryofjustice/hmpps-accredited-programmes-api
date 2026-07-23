package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SubjectAccessRequestServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var subjectAccessRequestService: SubjectAccessRequestService

  @MockitoSpyBean
  private lateinit var staffRepository: StaffRepository

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
      assertThat(referrerUsername).isEqualTo("Doe")
      assertThat(primaryPomStaffSurname).isEqualTo("Doe")
      assertThat(secondaryPomStaffSurname).isNull()
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
      assertThat(auditUsername).isEqualTo("Doe")
      assertThat(referrerUsername).isEqualTo("Doe")
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

  /**
   * APG-2495 A1 — regression guard for the post-APG-2492 batch staff-surname
   * resolver. When a subject has zero referrals, participations, audits and
   * status-history rows, `resolveStaffSurnames()` is invoked with empty input
   * sets. It must short-circuit cleanly (no empty `IN ()` clause, no NPE) and
   * every SAR collection must serialise as an empty list. The `person` block
   * still populates from the standalone `person` row.
   *
   * Chosen PRN mirrors the dev-DB retest subject captured in
   * `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`.
   */
  @Test
  fun `A1 — subject with a person row but no referrals returns populated person and empty collections`() {
    val prisonNumber = "A4433DZ"

    persistenceHelper.clearAllTableContent()
    persistenceHelper.createPerson(
      prisonNumber = prisonNumber,
      forename = "JOAN",
      surname = "BALISTRERI",
      location = "Moorland (HMP & YOI)",
      gender = "Female",
    )

    val result = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)

    assertNotNull(result)
    val content = result!!.content as SubjectAccessRequestService.Content

    // The whole point of A1: every collection must be empty, no exceptions thrown.
    assertThat(content.referrals).isEmpty()
    assertThat(content.courseParticipation).isEmpty()
    assertThat(content.auditRecords).isEmpty()
    assertThat(content.courses).isEmpty()
    assertThat(content.pniResults).isEmpty()
    assertThat(content.oasysPniResults).isEmpty()
    assertThat(content.referralStatusHistory).isEmpty()
    assertThat(content.referralStatusReasons).isEmpty()
    assertThat(content.selectedSexualOffenceDetails).isEmpty()
    assertThat(content.sexualOffenceDetails).isEmpty()
    assertThat(content.staff).isEmpty()
    assertThat(content.organisations).isEmpty()

    assertThat(content.person).isNotNull
    with(content.person!!) {
      assertThat(this.prisonNumber).isEqualTo(prisonNumber)
      assertThat(forename).isEqualTo("JOAN")
      assertThat(surname).isEqualTo("BALISTRERI")
      assertThat(location).isEqualTo("Moorland (HMP & YOI)")
      assertThat(gender).isEqualTo("Female")
    }
  }

  /**
   * APG-2495 A1 (companion) — a totally-unknown PRN with no rows anywhere,
   * including the `person` table. Proves the SAR responder yields a
   * well-formed empty payload rather than 500ing when the batch staff-surname
   * resolver is called with empty input sets AND `personRepository` returns
   * null.
   */
  @Test
  fun `A1 — unknown subject with no rows anywhere returns empty content and null person`() {
    persistenceHelper.clearAllTableContent()

    val result = subjectAccessRequestService.getPrisonContentFor("Z9999ZZ", null, null)

    assertNotNull(result)
    val content = result!!.content as SubjectAccessRequestService.Content

    assertThat(content.referrals).isEmpty()
    assertThat(content.courseParticipation).isEmpty()
    assertThat(content.auditRecords).isEmpty()
    assertThat(content.courses).isEmpty()
    assertThat(content.pniResults).isEmpty()
    assertThat(content.oasysPniResults).isEmpty()
    assertThat(content.referralStatusHistory).isEmpty()
    assertThat(content.referralStatusReasons).isEmpty()
    assertThat(content.selectedSexualOffenceDetails).isEmpty()
    assertThat(content.sexualOffenceDetails).isEmpty()
    assertThat(content.staff).isEmpty()
    assertThat(content.organisations).isEmpty()

    assertThat(content.person).isNull()
  }

  /**
   * APG-2495 B1 — post-APG-2492 batch-resolver query-count regression guard.
   *
   * The pre-APG-2492 code path resolved staff surnames per row, producing an
   * `O(referrals + participations × 2 + audits × 2 + statusHistory)` number of
   * point-lookup queries against `staff`. The batch resolver introduced in
   * APG-2492 (`StaffLookupService.resolveSurnamesByUsername/ByStaffId`)
   * collapses that into two batch queries — one by-username, one by-staff-id —
   * plus a single `findByPrisonNumber` for the `Content.staff` array.
   *
   * B1 asserts the query count is **exactly 3 `StaffRepository` calls per
   * SAR**, regardless of how many rows the subject has. If a future change
   * accidentally re-introduces per-row lookups, `verifyNoMoreInteractions`
   * will catch it and this test fails loudly.
   *
   * Test subject shape: multi-referral, multi-participation, multi-audit,
   * multi-status-history. The seed is deliberately chatty so that if
   * batching were disabled we'd see ≫ 3 calls.
   */
  @Test
  fun `B1 — SAR generation performs exactly 3 staff-repository calls regardless of row count`() {
    val prisonNumber = "A8610DY"
    val courseId = UUID.randomUUID()
    val offeringId = UUID.randomUUID()

    val referralIds = List(3) { UUID.randomUUID() }
    val referrerUsernames = listOf("REFERRER_A", "REFERRER_B", "REFERRER_C")
    val primaryPomStaffIds = listOf("101".toBigInteger(), "102".toBigInteger(), "103".toBigInteger())
    val secondaryPomStaffIds = listOf("201".toBigInteger(), "202".toBigInteger(), "203".toBigInteger())
    val cpAuthorUsernames = listOf("CP_AUTHOR_1", "CP_AUTHOR_2", "CP_AUTHOR_3")
    val auditActorUsernames = listOf("AUDIT_ACTOR_1", "AUDIT_ACTOR_2")

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

    // Referrers and 3 referrals — each with distinct referrer + POM staff ids.
    referrerUsernames.forEach { persistenceHelper.createReferrerUser(it) }
    referralIds.forEachIndexed { idx, referralId ->
      persistenceHelper.createReferral(
        referralId = referralId,
        offeringId = offeringId,
        prisonNumber = prisonNumber,
        referrerUsername = referrerUsernames[idx],
        additionalInformation = "info $idx",
        oasysConfirmed = true,
        hasReviewedProgrammeHistory = true,
        status = "REFERRAL_STARTED",
        submittedOn = LocalDateTime.now(),
        primaryPomStaffId = primaryPomStaffIds[idx],
        secondaryPomStaffId = secondaryPomStaffIds[idx],
      )
    }

    // 3 course participations with distinct created-by / last-modified-by users.
    cpAuthorUsernames.forEachIndexed { idx, author ->
      persistenceHelper.createCourseParticipation(
        participationId = UUID.randomUUID(),
        referralId = referralIds[idx % referralIds.size],
        prisonNumber = prisonNumber,
        courseName = "Course $idx",
        source = "source-$idx",
        detail = "detail-$idx",
        location = "location-$idx",
        type = if (idx % 2 == 0) CourseSetting.COMMUNITY.name else CourseSetting.CUSTODY.name,
        outcomeStatus = if (idx % 2 == 0) "COMPLETE" else "INCOMPLETE",
        yearStarted = 2020 + idx,
        yearCompleted = 2021 + idx,
        createdByUsername = author,
        createdDateTime = LocalDateTime.now(),
        lastModifiedByUsername = author,
        lastModifiedDateTime = LocalDateTime.now(),
      )
    }

    // 2 audit records with distinct actors.
    auditActorUsernames.forEach { actor ->
      persistenceHelper.createAuditRecord(
        prisonNumber = prisonNumber,
        auditAction = AuditAction.CREATE_REFERRAL.name,
        auditUsername = actor,
        referrerUsername = actor,
      )
    }

    // 3 status-history rows, each mapping to one of the referrers.
    referralIds.forEachIndexed { idx, referralId ->
      persistenceHelper.createReferralStatusHistory(
        referralId = referralId,
        username = referrerUsernames[idx],
        status = "REFERRAL_STARTED",
      )
    }

    // Backing staff rows so surnames actually resolve (proves the batch path
    // returns real values, not just short-circuits).
    val allUsernames = referrerUsernames + cpAuthorUsernames + auditActorUsernames
    allUsernames.forEachIndexed { idx, username ->
      persistenceHelper.createStaff(
        staffId = (1000 + idx).toBigInteger(),
        firstName = "First$idx",
        lastName = "Last$idx",
        username = username,
        primaryEmail = "user$idx@example.com",
      )
    }
    (primaryPomStaffIds + secondaryPomStaffIds).forEachIndexed { idx, staffId ->
      persistenceHelper.createStaff(
        staffId = staffId,
        firstName = "Pom$idx",
        lastName = "PomLast$idx",
        username = "POM_USER_$idx",
        primaryEmail = "pom$idx@example.com",
      )
    }

    // Also persist a person so `content.staff` (via findByPrisonNumber) has
    // rows to return — otherwise the query still runs but returns empty and
    // the batching evidence is less concrete.
    persistenceHelper.createPerson(
      prisonNumber = prisonNumber,
      forename = "TEST",
      surname = "SUBJECT",
    )

    // Reset spy — even though seed methods use native SQL and don't touch
    // the repository, be defensive: any pre-call interactions get zeroed.
    org.mockito.Mockito.clearInvocations(staffRepository)

    // WHEN — generate the SAR
    val result = subjectAccessRequestService.getPrisonContentFor(prisonNumber, null, null)

    // THEN — exactly 3 staff-repo calls
    assertNotNull(result)
    verify(staffRepository).findSurnamesByUsernames(anyCollection())
    verify(staffRepository).findSurnamesByStaffIds(anyCollection())
    verify(staffRepository).findByPrisonNumber(anyString())
    verifyNoMoreInteractions(staffRepository)
  }
}
