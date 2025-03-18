package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusHistory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional
class ReferralStatusHistoryService(
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val referralStatusRepository: ReferralStatusRepository,
) {

  fun getReferralStatusHistories(referralId: UUID) = referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralId)
    .filterNot { it.status.draft }
    .map {
      ReferralStatusHistory(
        id = it.id,
        referralId = it.referralId,
        status = it.status.code,
        statusDescription = it.status.description,
        statusColour = it.status.colour,
        previousStatus = it.previousStatus?.code,
        previousStatusDescription = it.previousStatus?.description,
        previousStatusColour = it.previousStatus?.colour,
        notes = it.notes,
        statusStartDate = it.statusStartDate.toInstant(ZoneOffset.UTC),
        username = it.username,
        categoryDescription = it.category?.description,
        reasonDescription = it.reason?.description,
      )
    }

  fun createReferralHistory(referral: ReferralEntity) {
    val status = referralStatusRepository.getByCode(referral.status)
    referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntity(
        referralId = referral.id!!,
        status = status,
      ),
    )
  }

  fun updateReferralHistory(
    referralId: UUID,
    previousStatusCode: String,
    newStatus: ReferralStatusEntity,
    newCategory: ReferralStatusCategoryEntity? = null,
    newReason: ReferralStatusReasonEntity? = null,
    newNotes: String? = null,
  ) {
    val previousStatus = referralStatusRepository.getByCode(previousStatusCode)
    val datetime = LocalDateTime.now()

    val statusHistory = referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralId)
    statusHistory.firstOrNull()?.let {
      it.statusEndDate = datetime
      it.durationAtThisStatus = ChronoUnit.MILLIS.between(it.statusStartDate, datetime)
    }

    referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntity(
        referralId = referralId,
        status = newStatus,
        previousStatus = previousStatus,
        category = newCategory,
        reason = newReason,
        notes = newNotes,
      ),
    )
  }
}
