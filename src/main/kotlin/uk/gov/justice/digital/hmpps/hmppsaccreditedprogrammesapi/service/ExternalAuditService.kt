package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService as ExternalAuditEventService

@Service
@Transactional
class ExternalAuditService(private val externalAuditEventService: ExternalAuditEventService) {
  fun publishAuditEvent(
    auditAction: String,
    userName: String?,
    prisonNumber: String?,
    subjectType: String? = "PRISON_NUMBER",
    referralId: String?,
  ) {
    externalAuditEventService.publishEvent(
      what = auditAction,
      who = userName,
      subjectId = prisonNumber,
      subjectType = subjectType,
      correlationId = referralId,
      service = "hmpps-accredited-programmes-api",
    )
  }

  fun publishExternalAuditEvent(referralEntity: ReferralEntity, auditAction: String) {
    publishAuditEvent(
      auditAction = auditAction,
      userName = referralEntity.referrer.username,
      prisonNumber = referralEntity.prisonNumber,
      referralId = referralEntity.id.toString(),
    )
  }
}
