package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.restapi

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferralsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.StartReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.ReferralsService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.transformer.toApi
import java.util.UUID

@Service
class ReferralsController(
  val referralsService: ReferralsService,
) : ReferralsApiDelegate {

  override fun referralsPost(startReferral: StartReferral): ResponseEntity<ReferralStarted> =
    referralsService.startReferral(
      prisonNumber = startReferral.prisonNumber,
      referrerId = startReferral.referrerId,
      offeringId = startReferral.offeringId,
    )?.let {
      ResponseEntity.ok(ReferralStarted(it))
    } ?: throw Exception("Unable to start referral")

  override fun referralsIdGet(id: UUID): ResponseEntity<Referral> =
    referralsService
      .getReferral(id)
      ?.let { ResponseEntity.ok(it.toApi()) }
      ?: throw NotFoundException("No Referral found at /referrals/$id")
}
