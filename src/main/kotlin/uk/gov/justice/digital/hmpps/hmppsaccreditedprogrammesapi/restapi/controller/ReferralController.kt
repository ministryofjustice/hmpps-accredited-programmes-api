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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralStatusHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SecurityService
import java.util.UUID

private const val DEFAULT_DIRECTION = "ascending"
private const val DEFAULT_SORT = "surname"

@Service
class ReferralController
@Autowired
constructor(
  private val referralService: ReferralService,
  private val securityService: SecurityService,
  private val referenceDataService: ReferralReferenceDataService,
  private val referralStatusHistoryService: ReferralStatusHistoryService,

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
      ?.let {
        val status = referenceDataService.getReferralStatus(it.status)
        ResponseEntity.ok(it.toApi(status))
      }
      ?: throw NotFoundException("No Referral found at /referrals/$id")

  override fun updateReferralById(id: UUID, referralUpdate: ReferralUpdate): ResponseEntity<Unit> {
    referralService.updateReferralById(id, referralUpdate.toDomain())
    return ResponseEntity.noContent().build()
  }

  override fun updateReferralStatusById(id: UUID, referralStatusUpdate: ReferralStatusUpdate): ResponseEntity<Unit> =
    with(referralStatusUpdate) {
      referralService.updateReferralStatusById(id, status.uppercase())
      ResponseEntity.noContent().build()
    }

  override fun submitReferralById(id: UUID): ResponseEntity<Unit> {
    referralService.getReferralById(id)?.let {
      referralService.submitReferralById(id)
      return ResponseEntity.noContent().build()
    } ?: throw NotFoundException("No referral found at /referral/$id")
  }

  override fun getReferralViewsByOrganisationId(
    organisationId: String,
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "10") size: Int,
    @RequestParam(value = "status", required = false) status: List<String>?,
    @RequestParam(value = "audience", required = false) audience: String?,
    @RequestParam(value = "courseName", required = false) courseName: String?,
    @RequestParam(value = "statusGroup", required = false) statusGroup: String?,
    @RequestParam(value = "sortColumn", required = false) sortColumn: String?,
    @RequestParam(value = "sortDirection", required = false) sortDirection: String?,
  ): ResponseEntity<PaginatedReferralView> {
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT, sortDirection ?: DEFAULT_DIRECTION))
    val apiReferralSummaryPage =
      referralService.getReferralViewByOrganisationId(organisationId, pageable, status, audience, courseName, statusGroup)

    return ResponseEntity.ok(
      PaginatedReferralView(
        content = apiReferralSummaryPage.content.map { it.toApi() },
        totalPages = apiReferralSummaryPage.totalPages,
        totalElements = apiReferralSummaryPage.totalElements.toInt(),
        pageSize = apiReferralSummaryPage.size,
        pageNumber = apiReferralSummaryPage.number,
        pageIsEmpty = apiReferralSummaryPage.isEmpty,
      ),
    )
  }

  override fun getReferralViewsByCurrentUser(
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "10") size: Int,
    @RequestParam(value = "status", required = false) status: List<String>?,
    @RequestParam(value = "audience", required = false) audience: String?,
    @RequestParam(value = "courseName", required = false) courseName: String?,
    @RequestParam(value = "statusGroup", required = false) statusGroup: String?,
    @RequestParam(value = "sortColumn", required = false) sortColumn: String?,
    @RequestParam(value = "sortDirection", required = false) sortDirection: String?,
  ): ResponseEntity<PaginatedReferralView> {
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT, sortDirection ?: DEFAULT_DIRECTION))
    val username: String =
      securityService.getCurrentUserName() ?: throw AccessDeniedException("unauthorised, username not present in token")

    val apiReferralSummaryPage =
      referralService.getReferralViewByUsername(username, pageable, status, audience, courseName, statusGroup)

    return ResponseEntity.ok(
      PaginatedReferralView(
        content = apiReferralSummaryPage.content.map { it.toApi() },
        totalPages = apiReferralSummaryPage.totalPages,
        totalElements = apiReferralSummaryPage.totalElements.toInt(),
        pageSize = apiReferralSummaryPage.size,
        pageNumber = apiReferralSummaryPage.number,
        pageIsEmpty = apiReferralSummaryPage.isEmpty,
      ),
    )
  }

  private fun getSortBy(sortColumn: String, sortDirection: String): Sort {
    return if (sortDirection == DEFAULT_DIRECTION) {
      Sort.by(sortColumn).ascending()
    } else {
      Sort.by(sortColumn).descending()
    }
  }

  override fun statusHistory(id: UUID): ResponseEntity<List<ReferralStatusHistory>> =
    ResponseEntity.ok(
      referralStatusHistoryService.getReferralStatusHistories(id),
    )
}
