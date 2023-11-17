package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.util.UUID

class ReferralEntityFactory : Factory<ReferralEntity> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var offeringId: Yielded<UUID> = { UUID.randomUUID() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var referrerId: Yielded<String> = { randomUppercaseAlphanumericString(6) }
  private var additionalInformation: Yielded<String?> = { null }
  private var oasysConfirmed: Yielded<Boolean> = { false }
  private var hasReviewedProgrammeHistory: Yielded<Boolean> = { false }
  private var status: Yielded<ReferralEntity.ReferralStatus> = { ReferralEntity.ReferralStatus.REFERRAL_STARTED }

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

  fun withAdditionalInformation(additionalInformation: String?) = apply {
    this.additionalInformation = { additionalInformation }
  }

  fun withOasysConfirmed(oasysConfirmed: Boolean) = apply {
    this.oasysConfirmed = { oasysConfirmed }
  }

  fun withHasReviewedProgrammeHistory(hasReviewedProgrammeHistory: Boolean) = apply {
    this.hasReviewedProgrammeHistory = { hasReviewedProgrammeHistory }
  }

  fun withStatus(status: ReferralEntity.ReferralStatus) = apply {
    this.status = { status }
  }

  override fun produce(): ReferralEntity = ReferralEntity(
    id = id(),
    offeringId = offeringId(),
    prisonNumber = prisonNumber(),
    referrerId = referrerId(),
    additionalInformation = additionalInformation(),
    oasysConfirmed = oasysConfirmed(),
    hasReviewedProgrammeHistory = hasReviewedProgrammeHistory(),
    status = status(),
  )
}
