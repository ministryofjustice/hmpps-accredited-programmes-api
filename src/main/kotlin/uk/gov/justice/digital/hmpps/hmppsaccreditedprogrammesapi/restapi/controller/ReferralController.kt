package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferralsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.authorization.UserMapper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import java.util.UUID

@Service
class ReferralController
@Autowired
constructor(
  private val referralService: ReferralService,
  private val userMapper: UserMapper,
) : ReferralsApiDelegate {

  override fun createReferral(referralCreate: ReferralCreate): ResponseEntity<ReferralCreated> =
    with(referralCreate) {
      referralService.createReferral(
        prisonNumber = prisonNumber,
        referrerId = referrerId,
        offeringId = offeringId,
      )?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(ReferralCreated(it))
      } ?: throw Exception("Unable to start referral")
    }

  override fun getReferralById(id: UUID): ResponseEntity<Referral> =
    referralService
      .getReferralById(id)
      ?.let { ResponseEntity.ok(it.toApi()) }
      ?: throw NotFoundException("No Referral found at /referrals/$id")

  override fun updateReferralById(id: UUID, referralUpdate: ReferralUpdate): ResponseEntity<Unit> {
    referralService.updateReferralById(id, referralUpdate.toDomain())
    return ResponseEntity.noContent().build()
  }

  override fun updateReferralStatusById(id: UUID, referralStatusUpdate: ReferralStatusUpdate): ResponseEntity<Unit> =
    with(referralStatusUpdate) {
      referralService.updateReferralStatusById(id, status.toDomain())
      ResponseEntity.noContent().build()
    }

  override fun submitReferralById(id: UUID): ResponseEntity<Unit> {
    referralService.getReferralById(id)?.let {
      referralService.submitReferralById(id)
      return ResponseEntity.noContent().build()
    } ?: throw NotFoundException("No referral found at /referral/$id")
  }

  override fun getReferralSummariesByOrganisationId(organisationId: String): ResponseEntity<List<ReferralSummary>> =
    referralService.getReferralsByOrganisationId(organisationId)
      .let { ResponseEntity.ok(it.map { referral -> referral.toReferralSummary() }) }
      ?: throw NotFoundException("No ReferralSummary found at /referrals/organisation/$organisationId/dashboard")

  override fun getReferralsForUser(): ResponseEntity<List<ReferralSummary>> =
    userMapper.getUsername()?.let { username ->
      referralService.getReferralForUser(username)
        .let { ResponseEntity.ok(it.map { referral -> referral.toReferralSummary() }) }
        ?: throw NotFoundException("No ReferralSummary found for username $username at /referrals/me/dashboard")
    } ?: throw NotFoundException("Could not find username from token")
}


