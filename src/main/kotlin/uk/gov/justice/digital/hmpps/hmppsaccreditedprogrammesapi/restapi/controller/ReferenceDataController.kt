package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService

@RestController
@Tag(
  name = "Reference data",
  description = """
    A series of endpoints for returning accredited programme reference data.
  """,
)
@RequestMapping("reference-data")
class ReferenceDataController(
  private val referenceDataService: ReferralReferenceDataService,
) {

  @GetMapping("/referral-statuses", produces = ["application/json"])
  fun getReferralStatuses(): List<ReferralStatusRefData> =
    referenceDataService
      .getReferralStatuses()

  @GetMapping("/referral-statuses/{code}", produces = ["application/json"])
  fun getReferralStatus(@PathVariable code: String): ReferralStatusRefData =
    referenceDataService
      .getReferralStatus(code)

  @GetMapping("/referral-statuses/{code}/categories", produces = ["application/json"])
  fun getReferralStatusCategories(@PathVariable code: String): List<ReferralStatusCategory> =
    referenceDataService
      .getReferralStatusCategories(code)

  @GetMapping("/referral-statuses/categories/{code}", produces = ["application/json"])
  fun getReferralStatusCategory(@PathVariable code: String): ReferralStatusCategory =
    referenceDataService
      .getReferralStatusCategory(code)

  @GetMapping(
    "/referral-statuses/{referralStatusCode}/categories/{categoryCode}/reasons",
    produces = ["application/json"],
  )
  fun getReferralStatusReasons(
    @PathVariable referralStatusCode: String,
    @PathVariable categoryCode: String,
    @RequestParam(defaultValue = "false") deselectAndKeepOpen: Boolean = false,
  ): List<ReferralStatusReason> =
    referenceDataService
      .getReferralStatusReasons(referralStatusCode, categoryCode, deselectAndKeepOpen)

  @GetMapping("/referral-statuses/categories/reasons/{code}", produces = ["application/json"])
  fun getReferralStatusReason(@PathVariable code: String): ReferralStatusReason =
    referenceDataService
      .getReferralStatusReason(code)

  @Operation(
    summary = "Gets a full list of Referral Status Reasons for a status type (WITHDRAWN or DESELECTED)",
    operationId = "getReferralStatusReasonsForReferralStatusType",
    description = """Get all Referral Status Reasons (code, description, referralCategoryCode) for the provided type""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successful operation", content = [Content(schema = Schema(implementation = ReferralStatusReason::class))]),
      ApiResponse(responseCode = "400", description = "No data exists for the provided type", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access person.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @GetMapping("/referral-statuses/{referralStatusType}/categories/reasons", produces = ["application/json"])
  fun getReferralStatusReasonsForReferralStatusType(
    @Parameter(description = "The referral status type (WITHDRAWN or DESELECTED)", required = true) @PathVariable referralStatusType: ReferralStatusType,
  ): List<ReferralStatusReason> = referenceDataService.getAllReferralStatusReasonsForType(referralStatusType)
}
