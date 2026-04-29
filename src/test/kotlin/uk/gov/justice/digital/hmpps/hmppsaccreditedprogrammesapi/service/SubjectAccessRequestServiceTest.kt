package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
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
import java.math.BigInteger
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
        .withAuditUsername("user1")
        .withReferrerUsername("user1")
        .produce(),
    )
    every { courseRepository.getSarCourses(prn) } returns listOf(
      CourseEntityFactory()
        .withDescription("course description")
        .withAudience("audience")
        .withIntensity("High intensity")
        .withAlternateName("Alternate name")
        .withListDisplayName("Drug Awareness")
        .produce(),
    )

    every { pniResultRepository.findAllByPrisonNumber(prn) } returns listOf(
      PniResultEntityFactory()
        .withPrisonNumber(prn)
        .withPniResultJson("{ \"status\": \"accepted\",}")
        .produce(),
    )
    every { referralStatusHistoryRepository.findByPrisonNumber(prn) } returns listOf(ReferralStatusHistoryEntityFactory().produce())
    every { staffRepository.findByPrisonNumber(prn) } returns listOf(StaffEntityFactory().produce())

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
      assertThat(referralStatusHistory.size).isEqualTo(1)
      assertThat(staff.size).isEqualTo(1)

      val referral = referrals[0]
      assertThat(referral.courseName).isEqualTo("Anger Management")
      assertThat(referral.referrerUsername).isEqualTo("user1")

      val participation = courseParticipation[0]
      assertThat(participation.courseName).isEqualTo("Drug Awareness")
      assertThat(participation.outcomeStatus).isEqualTo("INCOMPLETE")

      val audit = auditRecords[0]
      assertThat(audit.auditUsername).isEqualTo("user1")
      assertThat(audit.referrerUsername).isEqualTo("user1")

      val course = courses[0]
      assertThat(course.description).isEqualTo("course description")
      assertThat(course.audience).isEqualTo("audience")
      assertThat(course.intensity).isEqualTo("High intensity")
      assertThat(course.alternateName).isEqualTo("Alternate name")
      assertThat(course.listDisplayName).isEqualTo("Drug Awareness")

      val pniResult = pniResults[0]
      assertThat(pniResult.pniResultJson).isEqualTo("{ \"status\": \"accepted\",}")

      val referralStatusHistory = referralStatusHistory[0]
      assertThat(referralStatusHistory.version).isEqualTo(0)

      val staff = this.staff[0]
      assertThat(staff.id).isNotNull
      assertThat(staff.staffId).isEqualTo(BigInteger("487505"))
      assertThat(staff.username).isEqualTo("JDOE_ADM")
      assertThat(staff.firstName).isEqualTo("John")
      assertThat(staff.lastName).isEqualTo("Doe")
      assertThat(staff.primaryEmail).isEqualTo("john.doe@email.com")
    }

    verify { referralRepository.getSarReferrals(prn) }
    verify { courseParticipationRepository.getSarParticipations(prn) }
    verify { auditRepository.getSarAuditRecords(prn) }
    verify { courseRepository.getSarCourses(prn) }
    verify { pniResultRepository.findAllByPrisonNumber(prn) }
    verify { referralStatusHistoryRepository.findByPrisonNumber(prn) }
    verify { staffRepository.findByPrisonNumber(prn) }
  }
}
