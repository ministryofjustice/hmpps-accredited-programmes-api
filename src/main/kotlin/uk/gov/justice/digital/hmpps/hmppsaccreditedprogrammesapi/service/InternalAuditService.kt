package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import java.util.UUID

@Service
@Transactional
class InternalAuditService(private val auditRepository: AuditRepository, private val externalAuditService: ExternalAuditService) {
  fun createInternalAuditRecord(
    referralId: UUID? = null,
    prisonNumber: String,
    referrerUsername: String? = null,
    referralStatusFrom: String? = null,
    referralStatusTo: String? = null,
    courseId: UUID? = null,
    courseName: String? = null,
    courseLocation: String? = null,
    auditAction: AuditAction,
  ): UUID {
    return auditRepository.save(
      AuditEntity(
        referralId = referralId,
        prisonNumber = prisonNumber,
        referrerUsername = referrerUsername,
        referralStatusFrom = referralStatusFrom,
        referralStatusTo = referralStatusTo,
        courseId = courseId,
        courseName = courseName,
        courseLocation = courseLocation,
        auditAction = auditAction,
      ),
    ).id ?: throw Exception("Internal audit record creation failed for referralId: $referralId prisonNumber: $prisonNumber")
  }

  fun createInternalAuditRecord(referralEntity: ReferralEntity, currentStatus: String? = null) {
    externalAuditService.publishExternalAuditEvent(referralEntity, AuditAction.CREATE_REFERRAL.name)

    createInternalAuditRecord(
      referralId = referralEntity.id,
      prisonNumber = referralEntity.prisonNumber,
      referrerUsername = referralEntity.referrer.username,
      referralStatusFrom = currentStatus,
      referralStatusTo = referralEntity.status,
      courseId = referralEntity.offering.course.id,
      courseName = referralEntity.offering.course.name,
      courseLocation = referralEntity.offering.organisationId,
      auditAction = AuditAction.CREATE_REFERRAL,
    )
  }
}
