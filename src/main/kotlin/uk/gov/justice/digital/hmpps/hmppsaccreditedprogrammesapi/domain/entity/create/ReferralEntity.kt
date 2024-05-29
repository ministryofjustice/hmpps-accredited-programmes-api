package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral")
@SQLDelete(sql = "UPDATE referral SET deleted = true WHERE referral_id = ?")
data class ReferralEntity(
  @Id
  @GeneratedValue
  @Column(name = "referral_id")
  var id: UUID? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "offering_id", referencedColumnName = "offering_id")
  var offering: OfferingEntity,

  val prisonNumber: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referrer_username", referencedColumnName = "referrer_username")
  var referrer: ReferrerUserEntity,

  var additionalInformation: String? = null,

  var oasysConfirmed: Boolean = false,

  var hasReviewedProgrammeHistory: Boolean = false,

  var status: String = "REFERRAL_STARTED",

  var submittedOn: LocalDateTime? = null,

  var deleted: Boolean = false,
)
