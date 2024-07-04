package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferralsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ConfirmationFields
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
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
  private val auditService: AuditService,
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

  @Transactional
  override fun getReferralById(id: UUID, updatePerson: Boolean): ResponseEntity<Referral> =
    referralService
      .getReferralById(id, updatePerson)
      ?.let {
        auditService.audit(referralEntity = it, auditAction = AuditAction.VIEW_REFERRAL.name)
        val status = referenceDataService.getReferralStatus(it.status)
        ResponseEntity.ok(it.toApi(status))
      }
      ?: throw NotFoundException("No Referral found at /referrals/$id")

  @Transactional
  override fun deleteReferralById(id: UUID): ResponseEntity<Unit> {
    val referral = referralService.getReferralById(id)
      ?: throw NotFoundException("No Referral found to delete /referrals/$id")

    auditService.audit(referralEntity = referral, auditAction = AuditAction.DELETE_REFERRAL.name)

    val status = referenceDataService.getReferralStatus(referral.status)

    if (status.draft != true) {
      throw BusinessException("Only draft referrals can be deleted. Referral with $id has a status of ${status.code}")
    }

    referralService.deleteReferral(id)
    return ResponseEntity.noContent().build()
  }

  override fun updateReferralById(id: UUID, referralUpdate: ReferralUpdate): ResponseEntity<Unit> {
    referralService.updateReferralById(id, referralUpdate.toDomain())
    return ResponseEntity.noContent().build()
  }

  override fun updateReferralStatusById(id: UUID, referralStatusUpdate: ReferralStatusUpdate): ResponseEntity<Unit> {
    referralService.updateReferralStatusById(id, referralStatusUpdate)
    return ResponseEntity.noContent().build()
  }

  override fun submitReferralById(id: UUID): ResponseEntity<Unit> {
    referralService.getReferralById(id)?.let {
      referralService.submitReferralById(id)
      return ResponseEntity.noContent().build()
    } ?: throw NotFoundException("No referral found at /referral/$id")
  }

  override fun getReferralViewsByOrganisationId(
    organisationId: String,
    @RequestParam(value = "nameOrId", required = false) nameOrId: String?,
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
    val nameOrIdSearch = parseNameOrId(nameOrId)
    log.info("Organisation case list parsed name params: {}", nameOrIdSearch)
    val apiReferralSummaryPage =
      referralService.getReferralViewByOrganisationId(
        organisationId,
        nameOrIdSearch.prisonNumber,
        nameOrIdSearch.surnameOnly,
        nameOrIdSearch.forename,
        nameOrIdSearch.surname,
        pageable,
        status,
        audience,
        courseName,
        statusGroup,
      )

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

  private fun parseNameOrId(nameOrId: String?): NameOrIdSearch {
    val terms = nameOrId?.uppercase()?.split(" ") ?: return NameOrIdSearch()

    return when {
      terms.size == 1 -> {
        val term = terms[0]
        if (term.matches(Regex("[A-Za-z]\\d{4}[A-Za-z]{2}"))) {
          NameOrIdSearch(prisonNumber = term)
        } else {
          NameOrIdSearch(surnameOnly = term)
        }
      }

      terms.size > 1 -> {
        NameOrIdSearch(forename = terms[0], surname = terms[1])
      }

      else -> NameOrIdSearch()
    }
  }

  data class NameOrIdSearch(
    val prisonNumber: String = "",
    val surnameOnly: String = "",
    val forename: String = "",
    val surname: String = "",
  )

  override fun getReferralViewsByCurrentUser(
    @RequestParam(value = "page", defaultValue = "0") page: Int,
    @RequestParam(value = "size", defaultValue = "10") size: Int,
    @RequestParam(value = "nameOrId", required = false) nameOrId: String?,
    @RequestParam(value = "status", required = false) status: List<String>?,
    @RequestParam(value = "audience", required = false) audience: String?,
    @RequestParam(value = "courseName", required = false) courseName: String?,
    @RequestParam(value = "statusGroup", required = false) statusGroup: String?,
    @RequestParam(value = "sortColumn", required = false) sortColumn: String?,
    @RequestParam(value = "sortDirection", required = false) sortDirection: String?,
  ): ResponseEntity<PaginatedReferralView> {
    SecurityContextHolder.getContext().authentication?.authorities
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT, sortDirection ?: DEFAULT_DIRECTION))
    val username: String =
      securityService.getCurrentUserName() ?: throw AccessDeniedException("unauthorised, username not present in token")

    val nameOrIdSearch = parseNameOrId(nameOrId)
    log.info("Referal case list parsed name params: {}", nameOrIdSearch)

    val apiReferralSummaryPage =
      referralService.getReferralViewByUsername(
        username, pageable, status, audience, courseName, statusGroup, nameOrIdSearch.prisonNumber,
        nameOrIdSearch.surnameOnly,
        nameOrIdSearch.forename,
        nameOrIdSearch.surname,
      )

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

  override fun statusHistory(id: UUID, updatePerson: Boolean): ResponseEntity<List<ReferralStatusHistory>> {
    // This is just to update the person cache can remove this when the domain events are processed
    referralService
      .getReferralById(id, updatePerson)
    return ResponseEntity.ok(
      referralStatusHistoryService.getReferralStatusHistories(id),
    )
  }

  override fun getNextStatusTransitions(
    id: UUID,
    ptUser: Boolean,
    deselectAndKeepOpen: Boolean,
  ): ResponseEntity<List<ReferralStatusRefData>> {
    val referral = referralService.getReferralById(id)
    var statuses = referenceDataService.getNextStatusTransitions(referral!!.status, ptUser)
    // bespoke logic for deselect and keep open
    if (statuses.any { it.code == "DESELECTED" } && !deselectAndKeepOpen) {
      // rebuild the status list with a bespoke set of statuses
      val newStatusList = mutableListOf<ReferralStatusRefData>()
      newStatusList.addAll(statuses.filter { it.code == "PROGRAMME_COMPLETE" })
      newStatusList.add(
        statuses.first { it.code == "DESELECTED" }
          .copy(description = "Deselect and close referral", deselectAndKeepOpen = false),
      )
      newStatusList.add(
        statuses.first { it.code == "DESELECTED" }
          .copy(
            description = "Deselect and keep referral open",
            hintText = "This person cannot continue the programme now but may be able to in future.",
            deselectAndKeepOpen = true,
          ),
      )
      statuses = newStatusList
    }
    if (deselectAndKeepOpen) {
      // rebuild the status list with a bespoke set of statuses
      val newStatusList = mutableListOf<ReferralStatusRefData>()
      newStatusList.addAll(statuses.filter { it.code != "DESELECTED" && it.code != "PROGRAMME_COMPLETE" })
      statuses = newStatusList
    }

    return ResponseEntity.ok(
      statuses,
    )
  }

  override fun getConfirmationText(
    id: UUID,
    chosenStatusCode: String,
    ptUser: Boolean,
    deselectAndKeepOpen: Boolean,
  ): ResponseEntity<ConfirmationFields> {
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id '$id' not found")
    val currentStatus = referenceDataService.getReferralStatus(referral.status)
    val chosenStatus = referenceDataService.getReferralStatus(chosenStatusCode)
    var defaultConfirmationFields = ConfirmationFields(
      primaryHeading = "Move referral to ${chosenStatus.description.lowercase()}",
      primaryDescription = "Submitting this will change the status to ${chosenStatus.description.lowercase()}.",
      secondaryHeading = "Confirm status change",
      secondaryDescription = chosenStatus.confirmationText,
      warningText = when {
        currentStatus.code == "ON_PROGRAMME" && chosenStatus.code == "PROGRAMME_COMPLETE" -> {
          ""
        }

        chosenStatus.closed == true -> {
          "Submitting this will close the referral."
        }

        chosenStatus.hold == true -> {
          "Submitting this will pause the referral."
        }

        chosenStatus.release == true -> {
          "This will resume the referral."
        }

        else -> {
          ""
        }
      },
      hasConfirmation = currentStatus.hold != true && chosenStatus.hasConfirmation == true,
      notesOptional = chosenStatus.notesOptional,
    )

    // now see if there are any specific confirmation fields for this transition.
    val statusTransition = referenceDataService.getStatusTransition(referral.status, chosenStatusCode, ptUser)
    if (statusTransition != null) {
      defaultConfirmationFields = defaultConfirmationFields.copy(
        primaryHeading = statusTransition.primaryHeading ?: defaultConfirmationFields.primaryHeading,
        primaryDescription = statusTransition.primaryDescription ?: defaultConfirmationFields.primaryDescription,
        secondaryHeading = statusTransition.secondaryHeading ?: defaultConfirmationFields.secondaryHeading,
        secondaryDescription = statusTransition.secondaryDescription ?: defaultConfirmationFields.secondaryDescription,
        warningText = statusTransition.warningText ?: defaultConfirmationFields.warningText,
      )
    }

    // if current status is ON_PROGRAMME and deselect and keep open option is checked then change the status transitions again to the bespoke ones:
    if ((referral.status == "ON_PROGRAMME") && deselectAndKeepOpen && chosenStatusCode == "DESELECTED") {
      defaultConfirmationFields = ConfirmationFields(
        primaryHeading = "Deselection: keep referral open",
        primaryDescription = "This person cannot complete the programme now. They may be able to join or restart in the future.",
        secondaryHeading = "Choose the deselection status",
        secondaryDescription = "The referral will be paused at this status, for example Deselected - assessed as suitable.",
        warningText = "",
      )
    }

    return ResponseEntity.ok(
      defaultConfirmationFields,
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
