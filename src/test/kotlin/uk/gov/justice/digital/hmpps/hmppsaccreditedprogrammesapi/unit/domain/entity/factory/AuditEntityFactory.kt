package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import java.time.LocalDateTime
import java.util.UUID
class AuditEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var referralId: UUID? = UUID.randomUUID()
  private var prisonNumber: String = randomLowercaseString()
  private var referrerUsername: String = randomUppercaseString()
  private var referralStatusFrom: String? = randomUppercaseString()
  private var referralStatusTo: String? = randomUppercaseString()
  private var courseId: UUID? = UUID.randomUUID()
  private var courseName: String = randomUppercaseString()
  private var courseLocation: String = randomUppercaseString()
  private var auditAction: String = AuditAction.CREATE_REFERRAL.name
  private var auditUsername: String = randomUppercaseString()
  private var auditDateTime: LocalDateTime = LocalDateTime.now()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReferralId(referralId: UUID?) = apply { this.referralId = referralId }
  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withReferrerUsername(referrerUsername: String) = apply { this.referrerUsername = referrerUsername }
  fun withReferralStatusFrom(referralStatusFrom: String?) = apply { this.referralStatusFrom = referralStatusFrom }
  fun withReferralStatusTo(referralStatusTo: String?) = apply { this.referralStatusTo = referralStatusTo }
  fun withCourseId(courseId: UUID?) = apply { this.courseId = courseId }
  fun withCourseLocation(courseLocation: String) = apply { this.courseLocation = courseLocation }
  fun withCourseName(courseName: String) = apply { this.courseName = courseName }
  fun withAuditAction(auditAction: String) = apply { this.auditAction = auditAction }
  fun withAuditUsername(auditUsername: String) = apply { this.auditUsername = auditUsername }
  fun withAuditDateTime(auditDateTime: LocalDateTime) = apply { this.auditDateTime = auditDateTime }

  fun produce() = AuditEntity(
    id = this.id,
    referralId = this.referralId,
    prisonNumber = this.prisonNumber,
    referrerUsername = this.referrerUsername,
    referralStatusFrom = this.referralStatusFrom,
    referralStatusTo = this.referralStatusTo,
    courseId = this.courseId,
    courseName = this.courseName,
    courseLocation = this.courseLocation,
    auditAction = this.auditAction,
    auditUsername = this.auditUsername,
    auditDateTime = this.auditDateTime,
  )
}
