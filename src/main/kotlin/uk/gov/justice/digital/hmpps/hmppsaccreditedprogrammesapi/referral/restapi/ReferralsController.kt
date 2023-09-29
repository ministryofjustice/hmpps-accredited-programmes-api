package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferralsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StartReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralService
import java.util.UUID

@Service
class ReferralsController(
  val referralService: ReferralService,
) : ReferralsApiDelegate {

  override fun startReferral(startReferral: StartReferral): ResponseEntity<ReferralStarted> =
    with(startReferral) {
      referralService.startReferral(
        prisonNumber = prisonNumber,
        referrerId = referrerId,
        offeringId = offeringId,
      )?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(ReferralStarted(it))
      } ?: throw Exception("Unable to start referral")
    }

  override fun getReferral(id: UUID): ResponseEntity<Referral> =
    referralService
      .getReferral(id)
      ?.let { ResponseEntity.ok(it.toApi()) }
      ?: throw NotFoundException("No Referral found at /referrals/$id")

  override fun updateReferral(id: UUID, referralUpdate: ReferralUpdate): ResponseEntity<Unit> = with(referralUpdate) {
    referralService.updateReferral(id, reason, oasysConfirmed, hasReviewedProgrammeHistory)
    ResponseEntity.noContent().build()
  }

  override fun updateReferralStatus(id: UUID, statusUpdate: StatusUpdate): ResponseEntity<Unit> =
    with(statusUpdate) {
      referralService.updateReferralStatus(id, status.toDomain())
      ResponseEntity.noContent().build()
    }
}
