package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AuditEntityFactory
import java.util.*

class AuditServiceTest {

  @MockK(relaxed = true)
  private lateinit var auditRepository: AuditRepository

  private lateinit var auditService: AuditService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    auditService = AuditService(auditRepository)
  }

  @Test
  fun `create internal audit record successful`() {
    val auditEntity = AuditEntityFactory().withPrisonNumber { PRISON_LOCATION }.produce()

    every { auditRepository.save(any()) } returns auditEntity

    val createAuditRecord = auditService.createAuditRecord(
      referralId = UUID.randomUUID(),
      prisonNumber = PRISON_NUMBER_1,
      prisonerLocation = PRISON_LOCATION,
      referralStatusFrom = null,
      referralStatusTo = "REFERRAL_SUBMITTED",
      courseName = COURSE_NAME,
      courseLocation = COURSE_LOCATION,
      auditAction = AuditAction.CREATE_REFERRAL,
    )

    createAuditRecord shouldNotBe null
  }
}
