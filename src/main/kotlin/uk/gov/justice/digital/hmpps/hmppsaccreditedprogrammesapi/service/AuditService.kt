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
class AuditService(private val auditRepository: AuditRepository) {
  fun createAuditRecord(
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
    ).id ?: throw Exception("Audit creation failed")
  }

  fun createAuditRecord(referralEntity: ReferralEntity, currentStatus: String? = null) {
    createAuditRecord(
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
