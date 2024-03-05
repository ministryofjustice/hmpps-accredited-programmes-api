package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_LOCATION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.InternalAuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class AuditIntegrationTest {

  @Autowired
  lateinit var auditRepository: AuditRepository

  @Autowired
  lateinit var auditService: InternalAuditService

  @Test
  fun `Creating an interal audit record is successful with expected body`() {
    val offeringEntity = OfferingEntityFactory().produce()
    offeringEntity.course = CourseEntityFactory().produce()
    val referralEntity = ReferralEntityFactory().withOffering(offeringEntity).produce()

    auditService.createInternalAuditRecord(referralEntity, "Referral Submitted")

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == referralEntity.prisonNumber && it.auditAction == AuditAction.CREATE_REFERRAL }

    auditEntity.shouldNotBeNull()
    auditEntity.auditAction.shouldBeEqual(AuditAction.CREATE_REFERRAL)
    auditEntity.prisonNumber.shouldBeEqual(referralEntity.prisonNumber)
  }

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
