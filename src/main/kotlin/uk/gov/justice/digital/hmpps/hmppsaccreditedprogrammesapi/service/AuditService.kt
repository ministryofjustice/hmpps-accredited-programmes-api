package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant
import java.util.UUID

private const val ACCREDITED_PROGRAMME = "hmpps-accredited-programmes-api"

@Service
@Transactional
class AuditService(
  private val auditRepository: AuditRepository,
  private val hmppsAuditService: HmppsAuditService?,
) {
  private fun createInternalAuditRecord(
    referralId: UUID? = null,
    prisonNumber: String,
    referrerUsername: String? = null,
    referralStatusFrom: String? = null,
    referralStatusTo: String? = null,
    courseId: UUID? = null,
    courseName: String? = null,
    courseLocation: String? = null,
    auditAction: String,
  ) {
    auditRepository.save(
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
    ).id ?: { log.error("Failure to create internal audit record for $prisonNumber auditAction $auditAction") }
  }

  fun audit(prisonNumber: String, auditAction: String) {
    log.debug("AUDIT - Request received to perform $auditAction for prisonNumber $prisonNumber from ${getCurrentUser()}")
    createInternalAuditRecord(prisonNumber = prisonNumber, auditAction = auditAction)
    publishAuditEvent(prisonNumber = prisonNumber, auditAction = auditAction)
  }

  fun audit(referralEntity: ReferralEntity, currentStatus: String? = null, auditAction: String = AuditAction.CREATE_REFERRAL.name) {
    log.debug("AUDIT - Request received to perform $auditAction for prisonNumber ${referralEntity.prisonNumber} from ${getCurrentUser()}")
    createInternalAuditRecord(referralEntity, null, auditAction)
    publishAuditEvent(auditAction, referralEntity.prisonNumber, referralEntity.id.toString())
  }

  fun createInternalAuditRecord(referralEntity: ReferralEntity, currentStatus: String? = null, auditAction: String = AuditAction.CREATE_REFERRAL.name) {
    createInternalAuditRecord(
      referralId = referralEntity.id,
      prisonNumber = referralEntity.prisonNumber,
      referrerUsername = referralEntity.referrer.username,
      referralStatusFrom = currentStatus,
      referralStatusTo = referralEntity.status,
      courseId = referralEntity.offering.course.id,
      courseName = referralEntity.offering.course.name,
      courseLocation = referralEntity.offering.organisationId,
      auditAction = auditAction,
    )
  }

  private val scope = CoroutineScope(Dispatchers.IO)
  private fun publishAuditEvent(
    auditAction: String,
    prisonNumber: String?,
    subjectType: String? = "PRISON_NUMBER",
    referralId: String? = "",
  ) {
    log.debug("Audit service injected : ${hmppsAuditService != null} ")
    hmppsAuditService?.run {
      log.debug("Writing audit message for $prisonNumber ")
      scope.launch {
        publishEvent(
          what = auditAction,
          who = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
          `when` = Instant.now(),
          subjectType = subjectType,
          correlationId = referralId,
          service = ACCREDITED_PROGRAMME,
        )
      }
    }
  }

  private fun getCurrentUser() = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER"

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
