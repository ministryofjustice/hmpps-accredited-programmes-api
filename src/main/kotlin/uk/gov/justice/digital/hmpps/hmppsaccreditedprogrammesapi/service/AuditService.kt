package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import java.util.UUID

@Service
@Transactional
class AuditService(private val auditRepository: AuditRepository) {
  fun createAuditRecord(
    referralId: UUID?,
    prisonNumber: String,
    prisonerLocation: String?,
    referrerUsername: String?,
    referralStatusFrom: String?,
    referralStatusTo: String?,
    courseName: String?,
    courseLocation: String?,
    auditAction: AuditAction = AuditAction.CREATE,
  ): UUID {
    return auditRepository.save(
      AuditEntity(
        referralId = referralId,
        prisonNumber = prisonNumber,
        prisonerLocation = prisonerLocation,
        referrerUsername = referrerUsername,
        referralStatusFrom = referralStatusFrom,
        referralStatusTo = referralStatusTo,
        courseName = courseName,
        courseLocation = courseLocation,
        auditAction = auditAction,
      ),
    ).id ?: throw Exception("Audit creation failed")
  }
}
