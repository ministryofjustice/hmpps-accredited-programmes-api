package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferralsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SecurityService
import java.util.UUID

@Service
class ReferralController
@Autowired
constructor(
  private val referralService: ReferralService,
  private val securityService: SecurityService,
) : ReferralsApiDelegate {

  override fun createReferral(referralCreate: ReferralCreate): ResponseEntity<ReferralCreated> =
    with(referralCreate) {
      referralService.createReferral(
        prisonNumber = prisonNumber,
        offeringId = offeringId,
      )?.let {
        ResponseEntity.status(HttpStatus.CREATED).body(ReferralCreated(it))
      } ?: throw Exception("Unable to start referral")
    }

  override fun getReferralById(id: UUID, updatePerson: Boolean): ResponseEntity<Referral> =
    referralService
      .getReferralById(id, updatePerson)
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

  override fun getReferralSummariesByOrganisationId(
    organisationId: String,
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "10") size: Int,
    @RequestParam(value = "status", required = false) status: List<String>?,
    @RequestParam(value = "audience", required = false) audience: String?,
    @RequestParam(value = "courseName", required = false) courseName: String?,
  ): ResponseEntity<PaginatedReferralSummary> {
    val pageable = PageRequest.of(page, size, Sort.by("referralId"))
    val apiReferralSummaryPage = referralService.getReferralsByOrganisationId(organisationId, pageable, status, audience, courseName)

    return ResponseEntity.ok(
      PaginatedReferralSummary(
        content = apiReferralSummaryPage.content,
        totalPages = apiReferralSummaryPage.totalPages,
        totalElements = apiReferralSummaryPage.totalElements.toInt(),
        pageSize = apiReferralSummaryPage.size,
        pageNumber = apiReferralSummaryPage.number,
        pageIsEmpty = apiReferralSummaryPage.isEmpty,
      ),
    )
  }

  override fun getReferralSummariesByUsername(
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "10") size: Int,
    @RequestParam(value = "status", required = false) status: List<String>?,
    @RequestParam(value = "audience", required = false) audience: String?,
    @RequestParam(value = "courseName", required = false) courseName: String?,
  ): ResponseEntity<PaginatedReferralSummary> {
    val pageable = PageRequest.of(page, size, Sort.by("referralId"))
    val username: String = securityService.getCurrentUserName() ?: throw AccessDeniedException("unauthorised, username not present in token")
    val apiReferralSummaryPage = referralService.getReferralsByUsername(username, pageable, status, audience, courseName)

    return ResponseEntity.ok(
      PaginatedReferralSummary(
        content = apiReferralSummaryPage.content,
        totalPages = apiReferralSummaryPage.totalPages,
        totalElements = apiReferralSummaryPage.totalElements.toInt(),
        pageSize = apiReferralSummaryPage.size,
        pageNumber = apiReferralSummaryPage.number,
        pageIsEmpty = apiReferralSummaryPage.isEmpty,
      ),
    )
  }
}
