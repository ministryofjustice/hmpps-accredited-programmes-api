package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.getByCode
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

  fun getReferralStatusHistories(referralId: UUID): List<ReferralStatusHistory> {
    val history = referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralId)
    return history.map {
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
      )
    }
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

  fun updateReferralHistory(referral: ReferralEntity, previousStatusCode: String) {
    val status = referralStatusRepository.getByCode(referral.status)
    val previousStatus = referralStatusRepository.getByCode(previousStatusCode)
    val datetime = LocalDateTime.now()

    val statusHistory = referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referral.id!!)
    statusHistory.firstOrNull()?.let {
      it.statusEndDate = datetime
      it.durationAtThisStatus = ChronoUnit.MILLIS.between(datetime, it.statusStartDate)
    }

    referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntity(
        referralId = referral.id!!,
        status = status,
        previousStatus = previousStatus,
        statusEndDate = datetime,
      ),
    )
  }
}
