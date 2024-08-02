package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
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
@RequestMapping("referrals")
class ReferralsController(
  private val referralService: ReferralService,
  private val securityService: SecurityService,
  private val referenceDataService: ReferralReferenceDataService,
  private val referralStatusHistoryService: ReferralStatusHistoryService,
  private val auditService: AuditService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping("/{id}/status-transitions", produces = ["application/json"])
  fun getNextStatusTransitions(
    @PathVariable id: UUID,
    @RequestParam ptUser: Boolean,
    @RequestParam deselectAndKeepOpen: Boolean = false,
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
}
