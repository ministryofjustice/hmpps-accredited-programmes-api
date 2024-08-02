package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
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
}
