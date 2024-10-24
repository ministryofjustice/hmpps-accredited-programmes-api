package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AuditEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.util.*

class AuditServiceTest {

  @MockK(relaxed = true)
  private lateinit var auditRepository: AuditRepository

  @MockK(relaxed = true)
  private lateinit var hmppsAuditService: HmppsAuditService

  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    auditService = AuditService(auditRepository, hmppsAuditService)
  }

  @Test
  fun `create audit record from referral entity successful`() {
    val offering = OfferingEntityFactory().produce()
    offering.course = CourseEntityFactory().produce()
    offering.organisation = OrganisationEntityFactory().produce()

    val referralEntity = ReferralEntityFactory().withPrisonNumber(PRISON_NUMBER_1)
      .withOffering(offering).produce()

    every { auditRepository.save(any()) } returns AuditEntityFactory().produce()

    auditService.createInternalAuditRecord(referralEntity)

    verify {
      auditRepository.save(
        match {
          it.prisonNumber == referralEntity.prisonNumber &&
            it.auditAction == AuditAction.CREATE_REFERRAL.name
        },
      )
    }
  }
}
