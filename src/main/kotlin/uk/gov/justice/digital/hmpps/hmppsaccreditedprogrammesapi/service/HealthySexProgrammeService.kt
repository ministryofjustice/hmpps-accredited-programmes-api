package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SexualOffenceDetailsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.HspReferralDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexualOffenceDetails
import java.util.*

@Service
@Transactional
class HealthySexProgrammeService(
  private val referralService: ReferralService,
  private val sexualOffenceDetailsRepository: SexualOffenceDetailsRepository,

) {
  fun fetchHspDetailsForReferral(referralId: UUID): HspReferralDetails? = referralService.getReferralById(referralId)?.let {
    HspReferralDetails(
      prisonNumber = it.prisonNumber,
      eligibilityOverrideReason = it.eligibilityOverrideReasons.firstOrNull()?.reason,
      selectedOffences = getOffenceDetailsWithScores(it.selectedSexualOffenceDetails),
    )
  }

  private fun getOffenceDetailsWithScores(selectedOffences: Set<SelectedSexualOffenceDetailsEntity>): List<SexualOffenceDetails> {
    val selectedOffenceIds = selectedOffences.mapNotNull { it.sexualOffenceDetails?.id }

    return sexualOffenceDetailsRepository.findAll()
      .map { SexualOffenceDetails.from(it) }
      .map { offence ->
        if (offence.id !in selectedOffenceIds) {
          offence.apply { score = 0 }
        } else {
          offence
        }
      }
  }
}
