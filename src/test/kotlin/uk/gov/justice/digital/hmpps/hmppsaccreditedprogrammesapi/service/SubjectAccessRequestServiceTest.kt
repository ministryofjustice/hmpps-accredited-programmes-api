package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OasysPniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OasysPniResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PniResultRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AuditEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PniResultEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralStatusHistoryEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.StaffEntityFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class SubjectAccessRequestServiceTest {

  private val referralRepository: ReferralRepository = mockk()
  private val courseParticipationRepository: CourseParticipationRepository = mockk()
  private val auditRepository: AuditRepository = mockk()
  private val courseRepository: CourseRepository = mockk()
  private val pniResultRepository: PniResultRepository = mockk()
  private val personRepository: PersonRepository = mockk()
  private val oasysPniResultEntityRepository: OasysPniResultEntityRepository = mockk()
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository = mockk()
  private val staffRepository: StaffRepository = mockk()

  private lateinit var service: SubjectAccessRequestService

  @BeforeEach
  fun setup() {
    service = SubjectAccessRequestService(
      referralRepository,
      courseParticipationRepository,
      auditRepository,
      courseRepository,
      pniResultRepository,
      personRepository,
      oasysPniResultEntityRepository,
      referralStatusHistoryRepository,
      staffRepository,
    )
  }

  @Test
  fun `should return filtered and mapped prison content`() {
    // Given
    val prn = "A1234BC"
    val fromDate = LocalDate.of(2022, 1, 1)
    val toDate = LocalDate.of(2023, 1, 1)

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
        OfferingEntityFactory().withCourse(CourseEntityFactory().withName("Anger Management").produce()).produce(),
      )
      .withOriginalReferralId(UUID.randomUUID())
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

    every { referralRepository.getSarReferrals(prn) } returns listOf(referralEntity)
    every { courseParticipationRepository.getSarParticipations(prn) } returns listOf(participationEntity)
    every { auditRepository.getSarAuditRecords(prn) } returns listOf(
      AuditEntityFactory()
        .withPrisonNumber(prn)
        .withReferralStatusFrom("REFERRAL_STARTED")
        .withReferralStatusTo("REFERRAL_SUBMITTED")
        .withCourseName("Anger Management")
        .withCourseLocation("MDI")
        .withAuditAction("NOMIS_SEARCH_FOR_PERSON")
        .withAuditUsername("user1")
        .withReferrerUsername("user1")
        .produce(),
    )
    every { courseRepository.getSarCourses(prn) } returns listOf(
      CourseEntityFactory()
        .withName("Course Name")
        .produce(),
    )

    every { pniResultRepository.findAllByPrisonNumber(prn) } returns listOf(
      PniResultEntityFactory()
        .withPrisonNumber(prn)
        .withCrn("X1234")
        .withProgrammePathway("HIGH_INTENSITY")
        .withPniResultJson("{ \"status\": \"accepted\",}")
        .produce(),
    )
    every { personRepository.findPersonEntityByPrisonNumber(prn) } returns PersonEntity(
      id = UUID.randomUUID(),
      prisonNumber = prn,
      forename = "John",
      surname = "Doe",
      conditionalReleaseDate = LocalDate.of(2026, 1, 1),
      paroleEligibilityDate = LocalDate.of(2025, 1, 1),
      tariffExpiryDate = LocalDate.of(2027, 1, 1),
      earliestReleaseDate = LocalDate.of(2025, 1, 1),
      earliestReleaseDateType = "CRD",
      indeterminateSentence = false,
      nonDtoReleaseDateType = "CRD",
      sentenceType = "Determinate",
      location = "HMP Test",
      gender = "Male",
    )
    every { oasysPniResultEntityRepository.findAllByPrisonNumber(prn) } returns listOf(
      OasysPniResultEntity(
        pniResultId = UUID.randomUUID(),
        prisonNumber = prn,
        oasysAssessmentId = 123L,
        programmePathway = "ALTERNATIVE_PATHWAY",
      ),
    )
    every { referralStatusHistoryRepository.findByPrisonNumber(prn) } returns listOf(ReferralStatusHistoryEntityFactory().produce())
    every { staffRepository.findByPrisonNumber(prn) } returns listOf(
      StaffEntityFactory()
        .withStaffId("12345".toBigInteger())
        .withFirstName("Alex")
        .withLastName("River")
        .withPrimaryEmail("alex.river@justice.gov.uk")
        .withUsername("ARIVER")
        .produce(),
    )

    // When
    val result = service.getPrisonContentFor(prn, fromDate, toDate)

    // Then
    assertThat(result).isNotNull()
    with(result!!.content as SubjectAccessRequestService.Content) {
      assertThat(referrals.size).isEqualTo(1)
      assertThat(courseParticipation.size).isEqualTo(1)
      assertThat(auditRecords.size).isEqualTo(1)
      assertThat(courses.size).isEqualTo(1)
      assertThat(pniResults.size).isEqualTo(1)
      assertThat(person).isNotNull
      assertThat(oasysPniResults.size).isEqualTo(1)
      assertThat(referralStatusHistory.size).isEqualTo(1)
      assertThat(referralStatusReasons).isEmpty()
      assertThat(selectedSexualOffenceDetails).isEmpty()
      assertThat(sexualOffenceDetails).isEmpty()
      assertThat(staff).hasSize(1)

      val referral = referrals[0]
      assertThat(referral.referrerUsername).isEqualTo("user1")
      assertThat(referral.hasReviewedAdditionalInformation).isNull()

      val participation = courseParticipation[0]
      assertThat(participation.courseName).isEqualTo("Drug Awareness")
      assertThat(participation.otherCourseName).isEqualTo("Other course")
      assertThat(participation.outcomeStatus).isEqualTo("INCOMPLETE")
      assertThat(participation.outcomeDetail).isEqualTo("Outcome details")

      val audit = auditRecords[0]
      assertThat(audit.auditUsername).isEqualTo("user1")
      assertThat(audit.referrerUsername).isEqualTo("user1")
      assertThat(audit.auditAction).isEqualTo("NOMIS_SEARCH_FOR_PERSON")

      val course = courses[0]
      assertThat(course.name).isEqualTo("Course Name")

      val pniResult = pniResults[0]
      assertThat(pniResult.crn).isEqualTo("X1234")
      assertThat(pniResult.pniResultJson).isEqualTo("{ \"status\": \"accepted\",}")

      val person = person!!
      assertThat(person.prisonNumber).isEqualTo(prn)
      assertThat(person.forename).isEqualTo("John")

      val oasysPniResult = oasysPniResults[0]
      assertThat(oasysPniResult.prisonNumber).isEqualTo(prn)

      val referralStatusHistory = referralStatusHistory[0]
      assertThat(referralStatusHistory.status).isNotBlank()

      val staffMember = staff[0]
      assertThat(staffMember.lastName).isEqualTo("River")
    }

    verify { referralRepository.getSarReferrals(prn) }
    verify { courseParticipationRepository.getSarParticipations(prn) }
    verify { auditRepository.getSarAuditRecords(prn) }
    verify { courseRepository.getSarCourses(prn) }
    verify { pniResultRepository.findAllByPrisonNumber(prn) }
    verify { personRepository.findPersonEntityByPrisonNumber(prn) }
    verify { oasysPniResultEntityRepository.findAllByPrisonNumber(prn) }
    verify { referralStatusHistoryRepository.findByPrisonNumber(prn) }
    verify { staffRepository.findByPrisonNumber(prn) }
  }
}
