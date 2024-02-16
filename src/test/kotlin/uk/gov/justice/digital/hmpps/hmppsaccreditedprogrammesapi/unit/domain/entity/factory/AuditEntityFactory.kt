package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import java.time.LocalDateTime
import java.util.UUID

class AuditEntityFactory : Factory<AuditEntity> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var referralId: Yielded<UUID?> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomLowercaseString() }
  private var prisonLocation: Yielded<String> = { randomLowercaseString() }
  private var referrerUsername: Yielded<String> = { randomUppercaseString() }
  private var referralStatusFrom: Yielded<String?> = { randomUppercaseString() }
  private var referralStatusTo: Yielded<String?> = { randomUppercaseString() }
  private var courseId: Yielded<UUID?> = { UUID.randomUUID() }
  private var courseName: Yielded<String> = { randomUppercaseString() }
  private var courseLocation: Yielded<String> = { randomUppercaseString() }
  private var auditAction: Yielded<AuditAction> = { AuditAction.CREATE_REFERRAL }
  private var auditUsername: Yielded<String> = { randomUppercaseString() }
  private var auditDateTime: Yielded<LocalDateTime> = { LocalDateTime.now() }
  fun withId(id: () -> UUID?) = apply { this.id = id }
  fun withReferralId(referralId: () -> UUID?) = apply { this.referralId = referralId }
  fun withPrisonNumber(prisonNumber: () -> String) = apply { this.prisonNumber = prisonNumber }
  fun withPrisonLocation(prisonLocation: () -> String) = apply { this.prisonLocation = prisonLocation }
  fun withReferrerUsername(referrerUsername: () -> String) = apply { this.referrerUsername = referrerUsername }
  fun withReferralStatusFrom(referralStatusFrom: () -> String?) =
    apply { this.referralStatusFrom = referralStatusFrom }

  fun withReferralStatusTo(referralStatusTo: () -> String?) =
    apply { this.referralStatusTo = referralStatusTo }

  fun withCourseId(courseId: () -> UUID) = apply { this.courseId = courseId }
  fun withCourseLocation(courseLocation: () -> String) = apply { this.courseLocation = courseLocation }
  fun withCourseName(courseName: () -> String) = apply { this.courseName = courseName }
  fun withAuditAction(auditAction: () -> AuditAction) = apply { this.auditAction = auditAction }
  fun withAuditUsername(auditUsername: () -> String) = apply { this.auditUsername = auditUsername }
  fun withAuditDateTime(auditDateTime: () -> LocalDateTime) = apply { this.auditDateTime = auditDateTime }

  override fun produce() = AuditEntity(
    id = id(),
    referralId = referralId(),
    prisonNumber = prisonNumber(),
    prisonerLocation = prisonLocation(),
    referrerUsername = referrerUsername(),
    referralStatusFrom = referralStatusFrom(),
    referralStatusTo = referralStatusTo(),
    courseId = courseId(),
    courseName = courseName(),
    courseLocation = courseLocation(),
    auditAction = auditAction(),
    auditUsername = auditUsername(),
    auditDateTime = auditDateTime(),
  )
}
