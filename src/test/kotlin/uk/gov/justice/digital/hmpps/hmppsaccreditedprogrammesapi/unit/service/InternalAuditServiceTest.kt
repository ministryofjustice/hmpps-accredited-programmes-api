package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.InternalAuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AuditEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.*

class InternalAuditServiceTest {

  @MockK(relaxed = true)
  private lateinit var auditRepository: AuditRepository

  private lateinit var internalAuditService: InternalAuditService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    internalAuditService = InternalAuditService(auditRepository)
  }

  @Test
  fun `create internal audit record successful`() {
    val auditEntity = AuditEntityFactory().withPrisonNumber { PRISON_LOCATION }.produce()

    every { auditRepository.save(any()) } returns auditEntity

    val createAuditRecord = internalAuditService.createInternalAuditRecord(
      referralId = UUID.randomUUID(),
      prisonNumber = PRISON_NUMBER_1,
      referralStatusFrom = null,
      referralStatusTo = "REFERRAL_SUBMITTED",
      courseId = UUID.randomUUID(),
      courseName = COURSE_NAME,
      courseLocation = COURSE_LOCATION,
      auditAction = AuditAction.CREATE_REFERRAL,
    )

    createAuditRecord shouldNotBe null
  }

  @Test
  fun `create audit record from referral entity successful`() {
    val offering = OfferingEntityFactory().produce()
    offering.course = CourseEntityFactory().produce()

    val referralEntity = ReferralEntityFactory().withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering).produce()

    every { auditRepository.save(any()) } returns AuditEntityFactory().produce()

    internalAuditService.createInternalAuditRecord(referralEntity)

    verify {
      auditRepository.save(
        match {
          it.prisonNumber == referralEntity.prisonNumber &&
            it.auditAction == AuditAction.CREATE_REFERRAL
        },
      )
    }
  }
}
