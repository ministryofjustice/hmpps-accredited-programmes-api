package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.assertions.print.print
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ExternalAuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.InternalAuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class AuditIntegrationTest {

  @Autowired
  lateinit var auditRepository: AuditRepository

  @Autowired
  lateinit var internalAuditService: InternalAuditService

  @MockBean
  private lateinit var auditService: AuditService


  @Test
  fun `Creating an interal audit record is successful with expected body`() {

    ReferralEntityFactory()
      .withOffering(OfferingEntityFactory().produce())
      .with
      .produce()

    val referralEntity = ReferralEntityFactory().withOffering(OfferingEntityFactory().produce()).produce()


    internalAuditService.createInternalAuditRecord(referralEntity, AuditAction.CREATE_REFERRAL.name)

    val auditEntity = auditRepository.findAll()
      .firstOrNull { (it.prisonNumber == referralEntity.prisonNumber) && (it.auditAction.name == AuditAction.CREATE_REFERRAL.name) }

    auditEntity.shouldNotBeNull()
    auditEntity.auditAction.shouldBeEqual(AuditAction.CREATE_REFERRAL)
    auditEntity.prisonNumber.shouldBeEqual(referralEntity.prisonNumber)

  }
//
//  @Test
//  fun `Creating an interal audit record is successful with expected body`() {
//    val internalAuditRecord = createInternalAuditRecord(prisonNumber = PRISON_NUMBER_1)
//
//    val auditEntity = auditRepository.save(internalAuditRecord)
//
//    auditEntity.shouldNotBeNull()
//    auditEntity.auditAction.shouldBeEqual(AuditAction.CREATE_REFERRAL)
//    auditEntity.prisonNumber.shouldBeEqual(PRISON_NUMBER_1)
//  }

  private fun createInternalAuditRecord(
    referralId: UUID = UUID.randomUUID(),
    prisonNumber: String = PRISON_NUMBER_1,
    referrerUsername: String? = REFERRER_USERNAME,
    referralStatusFrom: String? = null,
    referralStatusTo: String? = "REFERRAL_STARTED",
    courseId: UUID = UUID.randomUUID(),
    courseName: String? = COURSE_NAME,
    courseLocation: String? = COURSE_LOCATION,
    auditAction: AuditAction = AuditAction.CREATE_REFERRAL,
  ): AuditEntity {
    return AuditEntity(
      referralId = referralId,
      prisonNumber = prisonNumber,
      referrerUsername = referrerUsername,
      referralStatusFrom = referralStatusFrom,
      referralStatusTo = referralStatusTo,
      courseId = courseId,
      courseName = courseName,
      courseLocation = courseLocation,
      auditAction = auditAction,
    )
  }
}
