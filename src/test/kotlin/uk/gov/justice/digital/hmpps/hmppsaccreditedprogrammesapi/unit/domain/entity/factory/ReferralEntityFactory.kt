package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralEntityFactory : Factory<ReferralEntity> {
  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var offering: Yielded<OfferingEntity> = { OfferingEntityFactory().produce() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var referrer: Yielded<ReferrerUserEntity> = { ReferrerUserEntityFactory().produce() }
  private var additionalInformation: Yielded<String?> = { null }
  private var oasysConfirmed: Yielded<Boolean> = { false }
  private var hasReviewedProgrammeHistory: Yielded<Boolean> = { false }
  private var status: Yielded<String> = { REFERRAL_STARTED }
  private var submittedOn: Yielded<LocalDateTime?> = { null }

  fun withId(id: UUID?) = apply {
    this.id = { id }
  }

  fun withOffering(offering: OfferingEntity) = apply {
    this.offering = { offering }
  }

  fun withPrisonNumber(prisonNumber: String) = apply {
    this.prisonNumber = { prisonNumber }
  }

  fun withReferrer(referrer: ReferrerUserEntity) = apply {
    this.referrer = { referrer }
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

  fun withStatus(status: String) = apply {
    this.status = { status }
  }

  override fun produce() = ReferralEntity(
    id = this.id(),
    offering = this.offering(),
    prisonNumber = this.prisonNumber(),
    referrer = this.referrer(),
    additionalInformation = this.additionalInformation(),
    oasysConfirmed = this.oasysConfirmed(),
    hasReviewedProgrammeHistory = this.hasReviewedProgrammeHistory(),
    status = this.status(),
    submittedOn = this.submittedOn(),
  )
}
