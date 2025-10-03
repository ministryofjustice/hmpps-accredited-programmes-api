package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test

class SubjectAccessRequestServiceTest {

  private val referralRepository: ReferralRepository = mockk()
  private val courseParticipationRepository: CourseParticipationRepository = mockk()
  private lateinit var service: SubjectAccessRequestService

  @BeforeEach
  fun setup() {
    service = SubjectAccessRequestService(referralRepository, courseParticipationRepository)
  }

  @Test
  fun `should return filtered and mapped prison content`() {
    // Given
    val prn = "A1234BC"
    val fromDate = LocalDate.of(2022, 1, 1)
    val toDate = LocalDate.of(2023, 1, 1)

    val referralEntity = ReferralEntity(
      prisonNumber = prn,
      oasysConfirmed = true,
      status = "COMPLETED",
      hasReviewedProgrammeHistory = true,
      additionalInformation = "Info",
      submittedOn = LocalDateTime.of(2022, 6, 1, 10, 0),
      referrerOverrideReason = "Override",
      referrer = ReferrerUserEntity(username = "user1"),
      offering = OfferingEntityFactory().withCourse(CourseEntityFactory().withName("Anger Management").produce()).produce(),
      originalReferralId = UUID.randomUUID(),
    )

    val participationEntity = CourseParticipationEntity(
      prisonNumber = prn,
      source = "SOURCE",
      setting = CourseParticipationSetting("REMOTE", CourseSetting.COMMUNITY),
      outcome = CourseParticipationOutcomeFactory().produce(),
      detail = "Details",
      courseName = "Drug Awareness",
      createdByUsername = "creator",
      createdDateTime = LocalDateTime.of(2022, 7, 1, 10, 0),
      lastModifiedByUsername = "modifier",
      lastModifiedDateTime = LocalDateTime.of(2022, 8, 1, 10, 0),
    )

    every { referralRepository.getSarReferrals(prn) } returns listOf(referralEntity)
    every { courseParticipationRepository.getSarParticipations(prn) } returns listOf(participationEntity)

    // When
    val result = service.getPrisonContentFor(prn, fromDate, toDate)

    // Then
    with(result!!.content as SubjectAccessRequestService.Content) {
      assertEquals(1, referrals.size)
      assertEquals(1, courseParticipation.size)

      val referral = referrals[0]
      assertEquals("Anger Management", referral.courseName)
      assertEquals("user1", referral.referrerUsername)

      val participation = courseParticipation[0]
      assertEquals("Drug Awareness", participation.courseName)
      assertEquals("INCOMPLETE", participation.outcomeStatus)
    }

    verify { referralRepository.getSarReferrals(prn) }
    verify { courseParticipationRepository.getSarParticipations(prn) }
  }
}
