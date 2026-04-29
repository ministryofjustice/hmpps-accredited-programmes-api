package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralStatusHistoryEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var version: Long = 0
  private var referralId: UUID = UUID.randomUUID()
  private var statusStartDate: LocalDateTime = LocalDateTime.now()
  private var username: String = randomUppercaseString()
  private var status: ReferralStatusEntity = ReferralStatusEntityFactory().produce()
  private var previousStatus: ReferralStatusEntity? = null
  private var category: ReferralStatusCategoryEntity? = null
  private var reason: ReferralStatusReasonEntity? = null
  private var notes: String? = null
  private var statusEndDate: LocalDateTime? = null
  private var durationAtThisStatus: Long? = null

  fun withId(id: UUID?) = apply { this.id = id }
  fun withVersion(version: Long) = apply { this.version = version }
  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withStatusStartDate(statusStartDate: LocalDateTime) = apply { this.statusStartDate = statusStartDate }
  fun withUsername(username: String) = apply { this.username = username }
  fun withStatus(status: ReferralStatusEntity) = apply { this.status = status }
  fun withPreviousStatus(previousStatus: ReferralStatusEntity?) = apply { this.previousStatus = previousStatus }
  fun withCategory(category: ReferralStatusCategoryEntity?) = apply { this.category = category }
  fun withReason(reason: ReferralStatusReasonEntity?) = apply { this.reason = reason }
  fun withNotes(notes: String?) = apply { this.notes = notes }
  fun withStatusEndDate(statusEndDate: LocalDateTime?) = apply { this.statusEndDate = statusEndDate }
  fun withDurationAtThisStatus(durationAtThisStatus: Long?) = apply { this.durationAtThisStatus = durationAtThisStatus }

  fun produce() = ReferralStatusHistoryEntity(
    id = this.id,
    version = this.version,
    referralId = this.referralId,
    statusStartDate = this.statusStartDate,
    username = this.username,
    status = this.status,
    previousStatus = this.previousStatus,
    category = this.category,
    reason = this.reason,
    notes = this.notes,
    statusEndDate = this.statusEndDate,
    durationAtThisStatus = this.durationAtThisStatus,
  )
}
