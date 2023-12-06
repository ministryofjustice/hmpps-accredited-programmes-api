package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import java.time.LocalDateTime
import java.util.*

class ReferralSummaryProjectionFactory : Factory<ReferralSummaryProjection> {

  private var referralId: Yielded<UUID> = { UUID.randomUUID() }
  private var courseName: Yielded<String> = { randomSentence(3..4, 6..8) }
  private var audience: Yielded<String> = { "Audience 1" }
  private var status: Yielded<ReferralEntity.ReferralStatus> = { ReferralEntity.ReferralStatus.REFERRAL_STARTED }
  private var submittedOn: Yielded<LocalDateTime> = { LocalDateTime.now() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var referrerUsername: Yielded<String> = { randomUppercaseString(9) }

  fun withReferralId(referralId: UUID) = apply {
    this.referralId = { referralId }
  }

  fun withCourseName(courseName: String) = apply {
    this.courseName = { courseName }
  }

  fun withAudience(audience: String) = apply {
    this.audience = { audience }
  }

  fun withStatus(status: ReferralEntity.ReferralStatus) = apply {
    this.status = { status }
  }

  fun withSubmittedOn(submittedOn: LocalDateTime) = apply {
    this.submittedOn = { submittedOn }
  }

  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }

  fun withReferrerUsername(referrerUsername: String) = apply {
    this.referrerUsername = { referrerUsername }
  }

  override fun produce() = ReferralSummaryProjection(
    referralId = this.referralId(),
    courseName = this.courseName(),
    audience = this.audience(),
    status = this.status(),
    submittedOn = this.submittedOn(),
    prisonNumber = this.prisonNumber(),
    referrerUsername = this.referrerUsername(),
  )
}
