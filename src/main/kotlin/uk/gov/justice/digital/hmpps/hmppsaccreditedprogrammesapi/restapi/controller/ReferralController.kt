package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ConfirmationFields
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.TransferReferralRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralStatusHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SecurityService
import java.util.UUID

@RestController
@Tag(
  name = "Referral",
  description = """
    A series of endpoints for returning accredited programme referrals.
  """,
)
@Transactional
class ReferralController(
  private val referralService: ReferralService,
  private val securityService: SecurityService,
  private val referenceDataService: ReferralReferenceDataService,
  private val referralStatusHistoryService: ReferralStatusHistoryService,
  private val auditService: AuditService,
  private val courseParticipationService: CourseParticipationService,
) {
  companion object {
    const val DEFAULT_DIRECTION = "ascending"
    const val DEFAULT_SORT_COLUMN = "surname"

    fun getSortBy(sortColumn: String, sortDirection: String): Sort {
      val primarySort = if (sortDirection == DEFAULT_DIRECTION) {
        Sort.by(sortColumn).ascending()
      } else {
        Sort.by(sortColumn).descending()
      }
      // Add ID as secondary sort for deterministic ordering
      return primarySort.and(Sort.by("id").ascending())
    }

    fun parseNameOrId(nameOrId: String?): NameOrIdSearch {
      val terms = nameOrId?.uppercase()?.split(",", " ")?.filter { it.trim().isNotEmpty() } ?: return NameOrIdSearch()

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
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["Referral"],
    summary = "Get the status transitions for a Referral",
    operationId = "getStatusTransitions",
    description = "Returns a list of status transitions for a Referral",
    security = [SecurityRequirement(name = "bearerAuth")],
    responses = [
      ApiResponse(
        responseCode = "404",
        description = "No Referral with ID",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "200",
        description = "Status transitions for a referral",
        content = [Content(schema = Schema(implementation = ReferralStatusRefData::class))],
      ),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}/status-transitions"],
    produces = ["application/json"],
  )
  fun getNextStatusTransitions(
    @PathVariable id: UUID,
    @RequestParam(defaultValue = "false") ptUser: Boolean = false,
    @RequestParam(defaultValue = "false") deselectAndKeepOpen: Boolean = false,
  ): ResponseEntity<List<ReferralStatusRefData>> {
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id $id not found")

    var statuses = referenceDataService.getNextStatusTransitions(referral.status, ptUser)
    val deselectedStatus = statuses.find { it.code == "DESELECTED" }

    if (deselectedStatus != null && !deselectAndKeepOpen) {
      // rebuild the status list with a bespoke set of statuses
      val newStatusList = mutableListOf<ReferralStatusRefData>()
      newStatusList.addAll(statuses.filter { it.code == "PROGRAMME_COMPLETE" })
      newStatusList.add(
        deselectedStatus
          .copy(description = "Deselect and close referral", deselectAndKeepOpen = false),
      )
      newStatusList.add(
        deselectedStatus
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

  @Operation(
    tags = ["Referrals"],
    summary = "Start a referral",
    operationId = "createReferral",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Started referral",
        content = [Content(schema = Schema(implementation = ReferralCreated::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "Conflict - Duplicate referral",
        content = [Content(schema = Schema(implementation = ReferralEntity::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/referrals"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun createReferral(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody referralCreate: ReferralCreate,
  ): ResponseEntity<Referral> {
    val duplicateReferrals =
      referralService.getDuplicateReferrals(referralCreate.prisonNumber, referralCreate.offeringId)

    if (!duplicateReferrals.isNullOrEmpty()) {
      log.info("Referral already exists for prisonNumber ${referralCreate.prisonNumber} and offering ${referralCreate.offeringId} ")
      return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateReferrals.first().toApi())
    }

    val createdReferral = referralService.createReferral(
      prisonNumber = referralCreate.prisonNumber,
      offeringId = referralCreate.offeringId,
      originalReferralId = referralCreate.originalReferralId,
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(createdReferral.toApi())
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Delete a draft referral",
    operationId = "deleteReferralById",
    description = """Deletes a draft referral by its ID.""",
    responses = [
      ApiResponse(responseCode = "204", description = "No Content - The referral was successfully deleted"),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Not authorised to access this endpoint/these referrals",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No referrals for supplied organisationId (Not Found).",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.DELETE],
    value = ["/referrals/{id}"],
  )
  fun deleteReferralById(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<Unit> {
    val referral = referralService.getReferralById(id)
      ?: throw NotFoundException("No Referral found to delete /referrals/$id")

    auditService.audit(referralEntity = referral, auditAction = AuditAction.DELETE_REFERRAL.name)

    val status = referenceDataService.getReferralStatus(referral.status)

    if (status.draft != true) {
      throw BusinessException("Only draft referrals can be deleted. Referral with $id has a status of ${status.code}")
    }

    courseParticipationService.deleteAllCourseParticipationsForReferral(id)
    referralService.deleteReferral(id)
    return ResponseEntity.noContent().build()
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Endpoint to present the UI with the appropriate text for the status change chosen.",
    operationId = "getConfirmationText",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The screen fields for the confirmation page",
        content = [Content(schema = Schema(implementation = ConfirmationFields::class))],
      ),
      ApiResponse(responseCode = "401", description = "The request was unauthorised"),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],

      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}/confirmation-text/{chosenStatusCode}"],
    produces = ["application/json"],
  )
  fun getConfirmationText(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
    @Parameter(
      description = "The status code that was chosen",
      required = true,
    ) @PathVariable("chosenStatusCode") chosenStatusCode: String,
    @Parameter(
      description = "is the user a Programme Team user",
      schema = Schema(defaultValue = "false"),
    ) @RequestParam(
      value = "ptUser",
      required = false,
      defaultValue = "false",
    ) ptUser: Boolean,
    @Parameter(
      description = "flag for bespoke deselection option",
      schema = Schema(defaultValue = "false"),
    ) @RequestParam(
      value = "deselectAndKeepOpen",
      required = false,
      defaultValue = "false",
    ) deselectAndKeepOpen: Boolean,
  ): ResponseEntity<ConfirmationFields> {
    val referral = referralService.getReferralById(id) ?: throw NotFoundException("Referral with id '$id' not found")
    val currentStatus = referenceDataService.getReferralStatus(referral.status)
    val chosenStatus = referenceDataService.getReferralStatus(chosenStatusCode)
    var defaultConfirmationFields = ConfirmationFields(
      primaryHeading = "Move referral to ${chosenStatus.description.lowercase()}",
      primaryDescription = "Submitting this will change the status to ${chosenStatus.description.lowercase()}.",
      secondaryHeading = chosenStatus.description,
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

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve a referral",
    operationId = "getReferralById",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the referral",
        content = [Content(schema = Schema(implementation = Referral::class))],
      ),
      ApiResponse(responseCode = "401", description = "The request was unauthorised"),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}"],
    produces = ["application/json"],
  )
  fun getReferralById(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<Referral> = ResponseEntity.ok(referralService.fetchCompleteReferralDataSetForId(id))

  @Operation(
    tags = ["Referrals"],
    summary = "Get paginated referrals for the current user",
    operationId = "getReferralViewsByCurrentUser",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paginated summary of referrals for an organisation",
        content = [Content(schema = Schema(implementation = PaginatedReferralView::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Not authorised to access this endpoint/these referrals",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No referrals for supplied organisationId (Not Found).",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/view/me/dashboard"],
    produces = ["application/json"],
  )
  fun getReferralViewsByCurrentUser(
    @Parameter(description = "Page number of the requested page", schema = Schema(defaultValue = "0")) @RequestParam(
      value = "page",
      required = false,
      defaultValue = "0",
    ) page: Int,
    @Parameter(
      description = "Number of items per page",
      schema = Schema(defaultValue = "10"),
    ) @RequestParam(value = "size", required = false, defaultValue = "10") size: Int,
    @Parameter(description = "The persons name: forename surname or surname or prison number. Name wll be a case insensitive like search. For example entering John will return people with the forename or surname containing the name john like johnathan. If two names are given then the assumption is that they are forename and surname.Forename and surname could include a comma between them (John,Smith) And if there is a single term that matches the regex for a prison number then only an exact prison number match will be carried out") @RequestParam(
      value = "nameOrId",
      required = false,
    ) nameOrId: String?,
    @Parameter(description = "Filter by the status of the referral") @RequestParam(
      value = "status",
      required = false,
    ) status: List<String>?,
    @Parameter(description = "Filter by the audience of the referral") @RequestParam(
      value = "audience",
      required = false,
    ) audience: String?,
    @Parameter(description = "Filter by the name of the course associated with this referral") @RequestParam(
      value = "courseName",
      required = false,
    ) courseName: String?,
    @Parameter(description = "Additional filter to only show \"open\", \"closed\" or \"draft\" referrals") @RequestParam(
      value = "statusGroup",
      required = false,
    ) statusGroup: String?,
    @Parameter(description = "Column to sort by default \"surname\"") @RequestParam(
      value = "sortColumn",
      required = false,
    ) sortColumn: String?,
    @Parameter(description = "Direction to sort by [ascending/descending] default \"ascending\"") @RequestParam(
      value = "sortDirection",
      required = false,
    ) sortDirection: String?,
    @Parameter(description = "When flag is true, then only ldc referrals are returned. If false or null is passed in all non ldc referrals are returned") @RequestParam(
      value = "hasLdc",
      required = false,
    ) hasLdc: Boolean?,
  ): ResponseEntity<PaginatedReferralView> {
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT_COLUMN, sortDirection ?: DEFAULT_DIRECTION))
    val username: String =
      securityService.getCurrentUserName() ?: throw AccessDeniedException("unauthorised, username not present in token")

    val nameOrIdSearch = parseNameOrId(nameOrId)

    val apiReferralSummaryPage =
      referralService.getReferralViewByUsername(
        username, pageable, status, audience, courseName, statusGroup, nameOrIdSearch.prisonNumber,
        nameOrIdSearch.surnameOnly,
        nameOrIdSearch.forename,
        nameOrIdSearch.surname,
        hasLdc,
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

  data class NameOrIdSearch(
    val prisonNumber: String = "",
    val surnameOnly: String = "",
    val forename: String = "",
    val surname: String = "",
  )

  @Operation(
    tags = ["Referrals"],
    summary = "Get paginated referrals by organisationId",
    operationId = "getReferralViewsByOrganisationId",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paginated summary of referrals for an organisation",
        content = [Content(schema = Schema(implementation = PaginatedReferralView::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Not authorised to access this endpoint/these referrals",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No referrals for supplied organisationId (Not Found).",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/view/organisation/{organisationId}/dashboard"],
    produces = ["application/json"],
  )
  fun getReferralViewsByOrganisationId(
    @Parameter(
      description = "The organisationId of an organisation",
      required = true,
    ) @PathVariable("organisationId") organisationId: String,
    @Parameter(description = "The persons name: forename surname or surname or prison number. Name wll be a case insensitive like search. For example entering John will return people with the forename or surname containing the name john like johnathan. If two names are given then the assumption is that they are forename and surname.Forename and surname could include a comma between them (John,Smith) And if there is a single term that matches the regex for a prison number then only an exact prison number match will be carried out") @RequestParam(
      value = "nameOrId",
      required = false,
    ) nameOrId: String?,
    @Parameter(description = "Page number of the requested page", schema = Schema(defaultValue = "0")) @RequestParam(
      value = "page",
      required = false,
      defaultValue = "0",
    ) page: Int,
    @Parameter(
      description = "Number of items per page",
      schema = Schema(defaultValue = "10"),
    ) @RequestParam(value = "size", required = false, defaultValue = "10") size: Int,
    @Parameter(description = "Filter by the status of the referral") @RequestParam(
      value = "status",
      required = false,
    ) status: List<String>?,
    @Parameter(description = "Filter by the audience of the referral") @RequestParam(
      value = "audience",
      required = false,
    ) audience: String?,
    @Parameter(description = "Filter by the name of the course associated with this referral") @RequestParam(
      value = "courseName",
      required = false,
    ) courseName: String?,
    @Parameter(description = "Additional filter to only show \"open\", \"closed\" or \"draft\" referrals") @RequestParam(
      value = "statusGroup",
      required = false,
    ) statusGroup: String?,
    @Parameter(description = "Column to sort by default \"surname\"") @RequestParam(
      value = "sortColumn",
      required = false,
    ) sortColumn: String?,
    @Parameter(description = "Direction to sort by [ascending/descending] default \"ascending\"") @RequestParam(
      value = "sortDirection",
      required = false,
    ) sortDirection: String?,
    @Parameter(description = "When flag is true, then only ldc referrals are returned. If false or null is passed in all non ldc referrals are returned") @RequestParam(
      value = "hasLdc",
      required = false,
    ) hasLdc: Boolean?,
  ): ResponseEntity<PaginatedReferralView> {
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT_COLUMN, sortDirection ?: DEFAULT_DIRECTION))
    val nameOrIdSearch = parseNameOrId(nameOrId)
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
        hasLdc,
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

  @Operation(
    tags = ["Referrals"],
    summary = "Returns the referral status history for this referral",
    operationId = "statusHistory",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "A list of status history",
        content = [Content(array = ArraySchema(schema = Schema(implementation = ReferralStatusHistory::class)))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorized to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}/status-history"],
    produces = ["application/json"],
  )
  fun statusHistory(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<List<ReferralStatusHistory>> = ResponseEntity.ok(
    referralStatusHistoryService.getReferralStatusHistories(id),
  )

  @Operation(
    tags = ["Referrals"],
    summary = "Submit a completed referral",
    operationId = "submitReferralById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "204", description = "Submitted a completed referral."),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden. The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "Conflict - Duplicate referral",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/referrals/{id}/submit"],
  )
  fun submitReferralById(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<Referral> {
    referralService.getReferralById(id)?.let {
      val duplicateReferrals = referralService.getDuplicateReferrals(it.prisonNumber, it.offering.id!!)
      if (!duplicateReferrals.isNullOrEmpty()) {
        log.info("Referral already exists for prisonNumber ${it.prisonNumber} and offering ${it.offering.id} ")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateReferrals.first().toApi())
      }

      val submittedReferral = referralService.submitReferralById(id)
      return ResponseEntity.status(HttpStatus.OK).body(submittedReferral.toApi())
    } ?: throw NotFoundException("No referral found for referral: $id")
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Update a referral",
    operationId = "updateReferralById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "204", description = "The referral was updated"),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/referrals/{id}"],
    consumes = ["application/json"],
  )
  fun updateReferralById(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
    @Parameter(description = "", required = true) @RequestBody referralUpdate: ReferralUpdate,
  ): ResponseEntity<Unit> {
    referralService.updateReferralById(id, referralUpdate.toDomain())
    return ResponseEntity.noContent().build()
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Change a referral's status",
    operationId = "updateReferralStatusById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "204", description = "The referral now has the requested status."),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "The referral may not change its status to the supplied value.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/referrals/{id}/status"],
    consumes = ["application/json"],
  )
  fun updateReferralStatusById(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody referralStatusUpdate: ReferralStatusUpdate,
  ): ResponseEntity<Unit> {
    referralService.updateReferralStatusById(id, referralStatusUpdate)
    return ResponseEntity.noContent().build()
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Transfer an existing referral to Building Choices",
    operationId = "transferReferralToBuildingChoices",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "The referral has now been transferred."),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "409",
        description = "The referral may not change its status to the supplied value.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "An error occurred when attempting to transfer the referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/referrals/transfer-to-building-choices"],
    consumes = ["application/json"],
  )
  fun transferReferralToBuildingChoices(
    @Parameter(
      description = "Details needed to transfer a referral to Building Choices",
      required = true,
    ) @RequestBody transferReferralRequest: TransferReferralRequest,
  ): ResponseEntity<Referral> {
    val newReferral = referralService.transferReferralToBuildingChoices(transferReferralRequest)
    return ResponseEntity.status(HttpStatus.OK).body(newReferral?.toApi())
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Fetch duplicate referrals based on prison number and offeringId",
    operationId = "getDuplicateReferrals",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paginated summary of referrals for an organisation",
        content = [Content(schema = Schema(implementation = Referral::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Not authorised to access this endpoint/these referrals",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "No referrals for supplied organisationId (Not Found).",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/duplicates"],
    produces = ["application/json"],
  )
  fun getDuplicateReferrals(
    @Parameter(description = "Prison number of person") @RequestParam(
      value = "prisonNumber",
      required = true,
    ) prisonNumber: String,
    @Parameter(description = "Offering id on where the programme is offered") @RequestParam(
      value = "offeringId",
      required = true,
    ) offeringId: UUID,
  ): ResponseEntity<List<Referral>?> {
    val duplicateReferrals = referralService.getDuplicateReferrals(prisonNumber, offeringId)
    if (duplicateReferrals.isNullOrEmpty()) {
      return ResponseEntity.noContent().build()
    }
    return ResponseEntity.ok(duplicateReferrals.map { referral -> referral.toApi() })
  }

  @Operation(
    tags = ["Referrals"],
    summary = "Retrieve other open referrals for a person",
    operationId = "getOtherOpenReferrals",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the referral",
        content = [Content(array = ArraySchema(schema = Schema(implementation = Referral::class)))],
      ),
      ApiResponse(responseCode = "401", description = "The request was unauthorised"),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access this referral.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The referral does not exist",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}/others"],
    produces = ["application/json"],
  )
  fun getOtherOpenReferrals(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<List<Referral>> = referralService.getReferralById(referralId = id)
    ?.let { it ->
      ResponseEntity.ok(
        referralService.getOpenReferralsForPerson(it.prisonNumber)
          .filterNot { referral -> referral.id == id }
          .map { it.toApi() },
      )
    } ?: throw NotFoundException("No Referral found for referralId $id")
}
