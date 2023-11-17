package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralEntityFactory : Factory<ReferralEntity> {

  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var offering: Yielded<OfferingEntity> = { OfferingEntityFactory().produce() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var referrerId: Yielded<String> = { randomUppercaseAlphanumericString(6) }
  private var additionalInformation: Yielded<String?> = { null }
  private var oasysConfirmed: Yielded<Boolean> = { false }
  private var hasReviewedProgrammeHistory: Yielded<Boolean> = { false }
  private var status: Yielded<ReferralEntity.ReferralStatus> = { ReferralEntity.ReferralStatus.REFERRAL_STARTED }
  private var submittedOn: Yielded<LocalDateTime> = { LocalDateTime.now() }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withOffering(offering: OfferingEntity) = apply {
    this.offering = { offering }
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

  fun withSubmittedOn(submittedOn: LocalDateTime) = apply {
    this.submittedOn = { submittedOn }
  }

  override fun produce(): ReferralEntity = ReferralEntity(
    id = id(),
    offering = offering(),
    prisonNumber = prisonNumber(),
    referrerId = referrerId(),
    additionalInformation = additionalInformation(),
    oasysConfirmed = oasysConfirmed(),
    hasReviewedProgrammeHistory = hasReviewedProgrammeHistory(),
    status = status(),
    submittedOn = submittedOn(),
  )
}
