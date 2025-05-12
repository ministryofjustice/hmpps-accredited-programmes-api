package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import java.util.UUID

@Entity
@Table(name = "selected_sexual_offence_details")
class SelectedSexualOffenceDetailsEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", referencedColumnName = "referral_id")
  var referral: ReferralEntity,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sexual_offence_details_id", referencedColumnName = "id")
  var sexualOffenceDetails: SexualOffenceDetailsEntity? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as SelectedSexualOffenceDetailsEntity
    return this.id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
}
