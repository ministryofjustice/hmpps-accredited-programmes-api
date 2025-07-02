package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.SQLRestriction
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral")
@SQLRestriction(value = "deleted = false")
class ReferralEntity(
  @Id
  @GeneratedValue
  @Column(name = "referral_id")
  var id: UUID? = null,

  @Version
  @Column(name = "version", nullable = false)
  var version: Long = 0,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offering_id", referencedColumnName = "offering_id")
  var offering: OfferingEntity,

  @Column(name = "prison_number")
  var prisonNumber: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referrer_username", referencedColumnName = "referrer_username")
  var referrer: ReferrerUserEntity,

  @Column(name = "additional_information")
  var additionalInformation: String? = null,

  @Column(name = "oasys_confirmed")
  var oasysConfirmed: Boolean = false,

  @Column(name = "has_reviewed_programme_history")
  var hasReviewedProgrammeHistory: Boolean = false,

  @Column(name = "has_reviewed_additional_information")
  var hasReviewedAdditionalInformation: Boolean? = null,

  @Column(name = "status")
  var status: String = "REFERRAL_STARTED",

  @Column(name = "submitted_on")
  var submittedOn: LocalDateTime? = null,

  @Column(name = "deleted")
  var deleted: Boolean = false,

  @Column(name = "primary_pom_staff_id")
  var primaryPomStaffId: BigInteger? = null,

  @Column(name = "secondary_pom_staff_id")
  var secondaryPomStaffId: BigInteger? = null,

  @Column(name = "referrer_override_reason")
  var referrerOverrideReason: String? = null,

  @Column(name = "original_referral_id")
  var originalReferralId: UUID? = null,

  @Column(name = "has_ldc")
  var hasLdc: Boolean? = null,

  @Column(name = "has_ldc_been_overridden_by_programme_team")
  var hasLdcBeenOverriddenByProgrammeTeam: Boolean = false,

  @OneToMany(mappedBy = "referral", fetch = FetchType.LAZY)
  var selectedSexualOffenceDetails: MutableSet<SelectedSexualOffenceDetailsEntity> = mutableSetOf(),

  @OneToMany(mappedBy = "referral", fetch = FetchType.LAZY)
  var eligibilityOverrideReasons: MutableSet<EligibilityOverrideReasonEntity> = mutableSetOf(),

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as ReferralEntity
    return this.id == other.id
  }

  override fun hashCode(): Int = id.hashCode()
  override fun toString(): String = "ReferralEntity(id=$id, version=$version, offering=$offering, prisonNumber='$prisonNumber', referrer=$referrer, additionalInformation=$additionalInformation, oasysConfirmed=$oasysConfirmed, hasReviewedProgrammeHistory=$hasReviewedProgrammeHistory, hasReviewedAdditionalInformation=$hasReviewedAdditionalInformation, status='$status', submittedOn=$submittedOn, deleted=$deleted, primaryPomStaffId=$primaryPomStaffId, secondaryPomStaffId=$secondaryPomStaffId, referrerOverrideReason=$referrerOverrideReason, originalReferralId=$originalReferralId, hasLdc=$hasLdc, hasLdcBeenOverriddenByProgrammeTeam=$hasLdcBeenOverriddenByProgrammeTeam, selectedSexualOffenceDetails=$selectedSexualOffenceDetails, eligibilityOverrideReasons=$eligibilityOverrideReasons)"
}
