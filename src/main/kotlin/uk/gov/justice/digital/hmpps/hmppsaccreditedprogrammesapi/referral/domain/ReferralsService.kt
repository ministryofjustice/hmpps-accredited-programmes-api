package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.jparepo.JpaReferralRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class ReferralsService(
  @Autowired val referralRepository: JpaReferralRepository,
) {
  fun startReferral(
    prisonNumber: String,
    offeringId: UUID,
    referrerId: String,
  ): UUID? = referralRepository.save(Referral(offeringId = offeringId, prisonNumber = prisonNumber, referrerId = referrerId)).id

  fun getReferral(referralId: UUID) = referralRepository.findById(referralId).getOrNull()
}
