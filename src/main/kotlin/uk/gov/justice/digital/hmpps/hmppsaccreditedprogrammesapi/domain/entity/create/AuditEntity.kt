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

  @Column(name = "referral_id")
  var referralId: UUID? = null,

  @Column(name = "prison_number", nullable = false)
  var prisonNumber: String,

  @Column(name = "prisoner_location")
  var prisonerLocation: String? = null,

  @Column(name = "referrer_username")
  var referrerUsername: String? = null,

  @Column(name = "referral_status_from")
  var referralStatusFrom: String? = null,

  @Column(name = "referral_status_to")
  var referralStatusTo: String? = null,

  @Column(name = "course_name")
  var courseName: String? = null,

  @Column(name = "courseLocation")
  var courseLocation: String? = null,

  @Column(name = "audit_action", nullable = false)
  var auditAction: AuditAction,

  @Column(name = "audit_username", nullable = false)
  var auditUsername: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @Column(name = "audit_date_time", nullable = false)
  var auditDateTime: LocalDateTime = LocalDateTime.now(),
)

enum class AuditAction {
  CREATE,
}
