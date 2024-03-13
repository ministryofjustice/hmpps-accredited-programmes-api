package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant

@Service
@Transactional
class ExternalAuditService(private val auditService: HmppsAuditService?) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val scope = CoroutineScope(Dispatchers.IO)
  fun publishAuditEvent(
    auditAction: String,
    userName: String,
    prisonNumber: String?,
    subjectType: String? = "PRISON_NUMBER",
    referralId: String? = "",
  ) {
    log.debug("Audit service injected : ${auditService != null} ")
    auditService?.run {
      log.debug("Writing audit message for $prisonNumber ")
      scope.launch {
        publishEvent(
          what = auditAction,
          who = userName,
          `when` = Instant.now(),
          subjectType = subjectType,
          correlationId = referralId,
          service = "hmpps-accredited-programmes-api",
        )
      }
    }
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
