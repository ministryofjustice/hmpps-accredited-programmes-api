package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomStringUpperCaseWithNumbers
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral
import java.util.UUID

class ReferralEntityFactory : Factory<Referral> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var offeringId: Yielded<UUID> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomStringUpperCaseWithNumbers(6) }
  private var referrerId: Yielded<String> = { randomStringUpperCaseWithNumbers(6) }
  private var reason: Yielded<String?> = { null }
  private var oasysConfirmed: Yielded<Boolean> = { false }
  private var status: Yielded<Referral.Status> = { Referral.Status.REFERRAL_STARTED }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }
  fun withOfferingId(offeringId: UUID) = apply {
    this.offeringId = { offeringId }
  }
  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }
  fun withReferrerId(referrerId: String) = apply {
    this.referrerId = { referrerId }
  }
  fun withReason(reason: String?) = apply {
    this.reason = { reason }
  }
  fun withOasysConfirmed(oasysConfirmed: Boolean) = apply {
    this.oasysConfirmed = { oasysConfirmed }
  }
  fun withStatus(status: Referral.Status) = apply {
    this.status = { status }
  }

  override fun produce(): Referral {
    return Referral(
      id = this.id(),
      offeringId = this.offeringId(),
      prisonNumber = this.prisonNumber(),
      referrerId = this.referrerId(),
      reason = this.reason(),
      oasysConfirmed = this.oasysConfirmed(),
      status = this.status(),
    )
  }
}
