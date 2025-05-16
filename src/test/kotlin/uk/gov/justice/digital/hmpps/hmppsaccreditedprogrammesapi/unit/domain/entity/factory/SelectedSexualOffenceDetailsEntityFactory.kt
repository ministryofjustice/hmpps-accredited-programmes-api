package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import java.util.UUID

class SelectedSexualOffenceDetailsEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var referral: ReferralEntity = ReferralEntityFactory().produce()
  private var sexualOffenceDetails: SexualOffenceDetailsEntity? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withSexualOffenceDetails(sexualOffenceDetails: SexualOffenceDetailsEntity?) = apply { this.sexualOffenceDetails = sexualOffenceDetails }

  fun produce() = SelectedSexualOffenceDetailsEntity(
    id = this.id,
    referral = this.referral,
    sexualOffenceDetails = this.sexualOffenceDetails,
  )
}
