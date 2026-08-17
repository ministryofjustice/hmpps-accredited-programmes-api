package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.StaffEntityFactory
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class SubjectAccessRequestServiceTest {

  private val referralRepository: ReferralRepository = mockk()
  private val courseParticipationRepository: CourseParticipationRepository = mockk()
  private val courseRepository: CourseRepository = mockk()
  private val staffRepository: StaffRepository = mockk()
  private val organisationRepository: OrganisationRepository = mockk()
  private val staffLookupService: StaffLookupService = mockk()

  private lateinit var service: SubjectAccessRequestService

  @BeforeEach
  fun setup() {
    service = SubjectAccessRequestService(
      referralRepository,
      courseParticipationRepository,
      courseRepository,
      staffRepository,
      organisationRepository,
      staffLookupService,
    )
    // By default resolve every username to "River" and every staff ID to an empty
    // result; individual tests can override. This mirrors the batch-lookup contract
    // where `resolveSurnamesByUsername` returns a map keyed by the requested
    // usernames and `resolveSurnamesByStaffId` returns an empty map when no staff
    // rows exist for the given IDs.
    every { staffLookupService.resolveSurnamesByUsername(any()) } answers {
      val usernames = firstArg<Collection<String?>>()
      usernames.asSequence()
        .filterNotNull()
        .filter { it.isNotBlank() }
        .toSet()
        .associateWith { "River" }
    }
    every { staffLookupService.resolveSurnamesByStaffId(any()) } returns emptyMap<BigInteger, String>()
  }

  @Test
  fun `should return filtered and mapped prison content`() {
    // Given
    val prn = "A1234BC"
    val fromDate = LocalDate.of(2022, 1, 1)
    val toDate = LocalDate.of(2023, 1, 1)

    val originalReferralId = UUID.fromString("11111111-2222-3333-4444-555555555555")
    val referralEntity = ReferralEntityFactory()
      .withPrisonNumber(prn)
      .withOasysConfirmed(true)
      .withStatus("COMPLETED")
      .withHasReviewedProgrammeHistory(true)
      .withAdditionalInformation("Info")
      .withSubmittedOn(LocalDateTime.of(2022, 6, 1, 10, 0))
      .withReferrerOverrideReason("Override")
      .withReferrer(ReferrerUserEntity(username = "user1"))
      .withOffering(
        OfferingEntityFactory()
          .withOrganisationId("MDI")
          .withCourse(CourseEntityFactory().withName("Anger Management").produce())
          .produce(),
      )
      .withOriginalReferralId(originalReferralId)
      .produce()

    // Seeded "original referral" that the parent's `originalReferralId` points at.
    // Every field is distinct from the parent so the SarOriginalReferral assertions
    // below prove the mapper is reading from the original entity rather than
    // leaking through parent state.
    val originalReferralEntity = ReferralEntityFactory()
      .withId(originalReferralId)
      .withPrisonNumber(prn)
      .withStatus("WITHDRAWN")
      .withSubmittedOn(LocalDateTime.of(2021, 3, 15, 9, 30))
      .withAdditionalInformation("Superseded original")
      .withReferrerOverrideReason("Original override")
      .withReferrer(ReferrerUserEntity(username = "original_user"))
      .withOffering(
        OfferingEntityFactory()
          .withOrganisationId("MDI")
          .withCourse(CourseEntityFactory().withName("Building Choices").produce())
          .produce(),
      )
      .withHasLdcBeenOverwrittenByProgrammeTeam(false)
      .withLdc(false)
      .produce()

    // Second referral: an `originalReferralId` that intentionally does NOT
    // resolve via `findAllById` – proves the defensive "unresolved -> null"
    // branch (logged as a WARN in production) and that a `null`
    // originalReferralId maps to a `null` originalReferral block.
    val orphanedOriginalId = UUID.fromString("77777777-8888-9999-aaaa-bbbbbbbbbbbb")
    val orphanReferralEntity = ReferralEntityFactory()
      .withPrisonNumber(prn)
      .withStatus("REFERRAL_STARTED")
      .withSubmittedOn(LocalDateTime.of(2022, 6, 2, 10, 0))
      .withReferrer(ReferrerUserEntity(username = "user2"))
      .withOffering(
        OfferingEntityFactory()
          .withOrganisationId("MDI")
          .withCourse(CourseEntityFactory().withName("Anger Management").produce())
          .produce(),
      )
      .withOriginalReferralId(orphanedOriginalId)
      .produce()
    val nullOriginalReferralEntity = ReferralEntityFactory()
      .withPrisonNumber(prn)
      .withStatus("REFERRAL_STARTED")
      .withSubmittedOn(LocalDateTime.of(2022, 6, 3, 10, 0))
      .withReferrer(ReferrerUserEntity(username = "user3"))
      .withOffering(
        OfferingEntityFactory()
          .withOrganisationId("MDI")
          .withCourse(CourseEntityFactory().withName("Anger Management").produce())
          .produce(),
      )
      .withOriginalReferralId(null)
      .produce()

    val participationEntity = CourseParticipationEntityFactory()
      .withPrisonNumber(prn)
      .withSource("SOURCE")
      .withSetting(CourseParticipationSetting("REMOTE", CourseSetting.COMMUNITY))
      .withOutcome(CourseParticipationOutcomeFactory().produce())
      .withDetail("Details")
      .withCourseName("Drug Awareness")
      .withOtherCourseName("Other course")
      .withOutcomeDetail("Outcome details")
      .withCreatedByUsername("creator")
      .withCreatedDateTime(LocalDateTime.of(2022, 7, 1, 10, 0))
      .withLastModifiedByUsername("modifier")
      .withLastModifiedDateTime(LocalDateTime.of(2022, 8, 1, 10, 0))
      .produce()

    every { referralRepository.getSarReferrals(prn) } returns listOf(referralEntity, orphanReferralEntity, nullOriginalReferralEntity)
    // Batch stub: seeded original resolves, orphaned original silently drops
    // out of the returned list (mirrors production IN-clause behaviour).
    every { referralRepository.findAllById(setOf(originalReferralId, orphanedOriginalId)) } returns listOf(originalReferralEntity)
    every { courseParticipationRepository.getSarParticipations(prn) } returns listOf(participationEntity)
    every { courseRepository.getSarCourses(prn) } returns listOf(
      CourseEntityFactory()
        .withName("Course Name")
        .produce(),
    )

    every { staffRepository.findByPrisonNumber(prn) } returns listOf(
      StaffEntityFactory()
        .withStaffId("12345".toBigInteger())
        .withFirstName("Alex")
        .withLastName("River")
        .withPrimaryEmail("alex.river@justice.gov.uk")
        .withUsername("ARIVER")
        .produce(),
    )
    every { organisationRepository.findAllByCodeIn(match { it.toSet() == setOf("MDI") }) } returns listOf(
      OrganisationEntityFactory()
        .withCode("MDI")
        .withName("HMP Moorland")
        .produce(),
    )

    // When
    val result = service.getPrisonContentFor(prn, fromDate, toDate)

    // Then
    assertThat(result).isNotNull()
    with(result!!.content as SubjectAccessRequestService.Content) {
      assertThat(referrals.size).isEqualTo(3)
      assertThat(courseParticipation.size).isEqualTo(1)
      assertThat(courses.size).isEqualTo(1)
      assertThat(staff).hasSize(1)

      val referral = referrals[0]
      assertThat(referral.referrerUsername).isEqualTo("River")
      assertThat(referral.primaryPomStaffSurname).isNull()
      assertThat(referral.secondaryPomStaffSurname).isNull()
      assertThat(referral.hasReviewedAdditionalInformation).isNull()
      // Parent-referral organisation is resolved from the same batch map that
      // powers SarOriginalReferral.organisationName, proving the field is
      // populated per-referral from `offering.organisationId` rather than a
      // separately fetched top-level list.
      assertThat(referral.organisationName).isEqualTo("HMP Moorland")

      // SarOriginalReferral – every field is sourced from the seeded original,
      // proving the mapper reads through `originalsById` rather than leaking
      // parent state, and that organisation-name / referrer-surname come from
      // the batch maps (not a separate lookup per original).
      val originalReferral = referral.originalReferral!!
      assertThat(originalReferral.courseName).isEqualTo("Building Choices")
      assertThat(originalReferral.organisationName).isEqualTo("HMP Moorland")
      assertThat(originalReferral.submittedOn).isEqualTo(LocalDateTime.of(2021, 3, 15, 9, 30))
      assertThat(originalReferral.statusCode).isEqualTo("WITHDRAWN")
      assertThat(originalReferral.referrerSurname).isEqualTo("River")
      assertThat(originalReferral.referrerOverrideReason).isEqualTo("Original override")
      assertThat(originalReferral.hasLdc).isEqualTo(false)
      assertThat(originalReferral.additionalInformation).isEqualTo("Superseded original")

      // Orphaned original-referral-id (referential-integrity drift): the
      // parent still exposes the raw UUID for debugging, but the nested block
      // is null because `findAllById` returned no row for it. A WARN is logged
      // in production (not asserted here to avoid coupling to a specific log
      // appender – the observable contract is the null nested block).
      val orphanReferral = referrals[1]
      assertThat(orphanReferral.originalReferral).isNull()

      val plainReferral = referrals[2]
      assertThat(plainReferral.originalReferral).isNull()

      val participation = courseParticipation[0]
      assertThat(participation.courseName).isEqualTo("Drug Awareness")
      assertThat(participation.otherCourseName).isEqualTo("Other course")
      assertThat(participation.outcomeStatus).isEqualTo("INCOMPLETE")
      assertThat(participation.outcomeDetail).isEqualTo("Outcome details")
      assertThat(participation.createdByUser).isEqualTo("River")
      assertThat(participation.updatedByUser).isEqualTo("River")

      val course = courses[0]
      assertThat(course.name).isEqualTo("Course Name")

      val staffMember = staff[0]
      assertThat(staffMember.lastName).isEqualTo("River")
    }

    verify { referralRepository.getSarReferrals(prn) }
    verify { courseParticipationRepository.getSarParticipations(prn) }
    verify { courseRepository.getSarCourses(prn) }
    verify { staffRepository.findByPrisonNumber(prn) }
  }
}
