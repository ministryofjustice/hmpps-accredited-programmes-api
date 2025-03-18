package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_status_history")
class ReferralStatusHistoryEntity(

  @Id
  @Column(name = "statusHistoryId")
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: UUID? = null,

  @Version
  @Column(name = "version", nullable = false)
  var version: Long = 0,

  var referralId: UUID,
  var statusStartDate: LocalDateTime = LocalDateTime.now(),
  var username: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",
  @ManyToOne
  @JoinColumn(name = "status")
  var status: ReferralStatusEntity,
  @ManyToOne
  @JoinColumn(name = "previousStatus")
  var previousStatus: ReferralStatusEntity? = null,
  @ManyToOne
  @JoinColumn(name = "category")
  var category: ReferralStatusCategoryEntity? = null,
  @ManyToOne
  @JoinColumn(name = "reason")
  var reason: ReferralStatusReasonEntity? = null,
  var notes: String? = null,
  var statusEndDate: LocalDateTime? = null,
  var durationAtThisStatus: Long? = null,
)


