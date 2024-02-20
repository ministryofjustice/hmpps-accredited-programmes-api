package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "audit_record")
class AuditEntity(

  @Id
  @Column(name = "audit_record_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: UUID? = null,
  var referralId: UUID? = null,
  var prisonNumber: String,
  var referrerUsername: String? = null,
  var referralStatusFrom: String? = null,
  var referralStatusTo: String? = null,
  var courseId: UUID? = null,
  var courseName: String? = null,
  var courseLocation: String? = null,
  var auditAction: AuditAction,
  var auditUsername: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
  var auditDateTime: LocalDateTime = LocalDateTime.now(),
)

enum class AuditAction {
  CREATE_REFERRAL,
  UPDATE_REFERRAL,
  SEARCH_FOR_PERSON,
}
