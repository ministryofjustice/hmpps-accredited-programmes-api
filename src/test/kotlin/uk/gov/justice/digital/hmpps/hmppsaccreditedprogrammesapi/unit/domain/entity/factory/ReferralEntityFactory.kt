package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import java.time.LocalDateTime
import java.util.UUID
class ReferralEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var offering: OfferingEntity = OfferingEntityFactory().produce()
  private var prisonNumber: String = randomPrisonNumber()
  private var referrer: ReferrerUserEntity = ReferrerUserEntityFactory().produce()
  private var additionalInformation: String? = null
  private var oasysConfirmed: Boolean = false
  private var hasReviewedProgrammeHistory: Boolean = false
  private var status: String = REFERRAL_STARTED
  private var submittedOn: LocalDateTime? = null
  private var referrerOverrideReason: String? = null
  private var hasLdc: Boolean = false
  private var hasLdcBeenOverwrittenByProgrammeTeam: Boolean = false

  fun withId(id: UUID?) = apply { this.id = id }
  fun withOffering(offering: OfferingEntity) = apply { this.offering = offering }
  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withReferrer(referrer: ReferrerUserEntity) = apply { this.referrer = referrer }
  fun withAdditionalInformation(additionalInformation: String?) = apply { this.additionalInformation = additionalInformation }
  fun withOasysConfirmed(oasysConfirmed: Boolean) = apply { this.oasysConfirmed = oasysConfirmed }
  fun withHasReviewedProgrammeHistory(hasReviewedProgrammeHistory: Boolean) = apply { this.hasReviewedProgrammeHistory = hasReviewedProgrammeHistory }
  fun withStatus(status: String) = apply { this.status = status }
  fun withReferrerOverrideReason(referrerOverrideReason: String?) = apply { this.referrerOverrideReason = referrerOverrideReason }
  fun withLdc(hasLdc: Boolean) = apply { this.hasLdc = hasLdc }
  fun withHasLdcBeenOverwrittenByProgrammeTeam(hasLdcBeenOverwrittenByProgrammeTeam: Boolean) = apply { this.hasLdcBeenOverwrittenByProgrammeTeam = hasLdcBeenOverwrittenByProgrammeTeam }

  fun produce() = ReferralEntity(
    id = this.id,
    offering = this.offering,
    prisonNumber = this.prisonNumber,
    referrer = this.referrer,
    additionalInformation = this.additionalInformation,
    oasysConfirmed = this.oasysConfirmed,
    hasReviewedProgrammeHistory = this.hasReviewedProgrammeHistory,
    status = this.status,
    submittedOn = this.submittedOn,
    referrerOverrideReason = this.referrerOverrideReason,
    hasLdc = this.hasLdc,
    hasLdcBeenOverriddenByProgrammeTeam = this.hasLdcBeenOverwrittenByProgrammeTeam,
  )
}
