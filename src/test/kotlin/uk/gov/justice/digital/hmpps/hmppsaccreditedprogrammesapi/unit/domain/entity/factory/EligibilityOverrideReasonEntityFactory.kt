package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EligibilityOverrideReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OverrideType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.util.UUID

class EligibilityOverrideReasonEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var reason: String = "Test override reason"
  private var overrideType: OverrideType = OverrideType.HEALTHY_SEX_PROGRAMME
  private var referral: ReferralEntity = ReferralEntityFactory().produce()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReason(reason: String) = apply { this.reason = reason }
  fun withOverrideType(overrideType: OverrideType) = apply { this.overrideType = overrideType }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }

  fun produce() = EligibilityOverrideReasonEntity(
    id = this.id,
    reason = this.reason,
    overrideType = this.overrideType,
    referral = this.referral,
  )
}
