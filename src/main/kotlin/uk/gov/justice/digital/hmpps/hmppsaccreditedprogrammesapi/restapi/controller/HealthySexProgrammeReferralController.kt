package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReferralController.Companion.DEFAULT_DIRECTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReferralController.Companion.DEFAULT_SORT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReferralController.Companion.getSortBy
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReferralController.Companion.parseNameOrId
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.HspReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.HspReferralDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.HealthySexProgrammeService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import java.util.UUID

@RestController
@Tag(
  name = "HSP Referrals",
  description = """
    A series of endpoints for the processing of Healthy Sex Programme referrals.
  """,
)
class HealthySexProgrammeReferralController(

  private val referralService: ReferralService,
  private val healthySexProgrammeService: HealthySexProgrammeService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Operation(
    tags = ["HSP Referrals"],
    summary = "Create an HSP referral",
    operationId = "createHealthySexProgrammeReferral",
    description = "Create a Healthy Sex Programme referral",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "Created HSP referral",
        content = [Content(schema = Schema(implementation = Referral::class))],
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
    value = ["/referral/hsp"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun createHealthySexProgrammeReferral(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody hspReferralCreate: HspReferralCreate,
  ): ResponseEntity<Referral> {
    if (hspReferralCreate.selectedOffences.isEmpty()) {
      log.info("No selected offences supplied in request body")
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
    }

    val duplicateReferrals =
      referralService.getDuplicateReferrals(hspReferralCreate.prisonNumber, hspReferralCreate.offeringId)

    if (!duplicateReferrals.isNullOrEmpty()) {
      log.info("Referral already exists for prisonNumber ${hspReferralCreate.prisonNumber} and offering ${hspReferralCreate.offeringId}")
      return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicateReferrals.first().toApi())
    }

    val createdReferral = referralService.createHspReferral(
      prisonNumber = hspReferralCreate.prisonNumber,
      offeringId = hspReferralCreate.offeringId,
      selectedOffenceIds = hspReferralCreate.selectedOffences,
      eligibilityOverrideReason = hspReferralCreate.eligibilityOverrideReason,
    )
    return ResponseEntity.status(HttpStatus.CREATED).body(createdReferral)
  }

  @Operation(
    tags = ["HSP Referrals"],
    summary = "Get paginated Healthy Sex Programmes(HSP) referrals ",
    operationId = "getReferralViewsByCurrentUser",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Paginated summary of HSP referrals",
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
        description = "No HSP referrals found (Not Found).",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/view/hsp/dashboard"],
    produces = ["application/json"],
  )
  fun getHspReferrals(
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
    @Parameter(description = "When flag is true, then only ldc referrals are returned. If false of null is passed in all non ldc referrals are returned") @RequestParam(
      value = "hasLdc",
      required = false,
    ) hasLdc: Boolean?,
  ): ResponseEntity<PaginatedReferralView> {
    val pageable = PageRequest.of(page, size, getSortBy(sortColumn ?: DEFAULT_SORT, sortDirection ?: DEFAULT_DIRECTION))

    val nameOrIdSearch = parseNameOrId(nameOrId)

    val apiReferralSummaryPage =
      referralService.getHspReferralsView(
        pageable = pageable,
        status = status,
        statusGroup = statusGroup,
        prisonNumber = nameOrIdSearch.prisonNumber,
        surnameOnly = nameOrIdSearch.surnameOnly,
        forename = nameOrIdSearch.forename,
        surname = nameOrIdSearch.surname,
        hasLdc = hasLdc,
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
    tags = ["HSP Referrals"],
    summary = "Retrieve the Healthy Sex Programmes(HSP) specific details associated with the referral",
    operationId = "getHealthySexProgrammeReferralDetails",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The HSP details associated with the referral",
        content = [Content(schema = Schema(implementation = HspReferralDetails::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Not authorised to access this endpoint",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The HSP referrals was not found.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/referrals/{id}/hsp-details"],
    produces = ["application/json"],
  )
  fun getHealthySexProgrammeReferralDetails(
    @Parameter(
      description = "The id (UUID) of a referral",
      required = true,
    ) @PathVariable("id") referralId: UUID,
  ): ResponseEntity<HspReferralDetails> = healthySexProgrammeService.fetchHspDetailsForReferral(referralId)?.let {
    ResponseEntity.ok(it)
  } ?: throw NotFoundException("No referral found for referral: $referralId")
}
