package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.UpdateAuditCaseNotesRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StaffService

@RestController
@RequestMapping("admin")
@Tag(
  name = "Admin",
  description = """
    This endpoint will refresh all of the prisoners within ACP - BE GENTLE.
  """,
)
class AdminController(
  private val personService: PersonService,
  private val referralService: ReferralService,
  private val staffService: StaffService,
  private val caseNotesApiService: CaseNotesApiService,
  private val auditService: AuditService,
) {
  @Operation(
    tags = ["Admin"],
    summary = "endpoint to update the cache in the person table. " +
      "This should sparingly as it updates all the people in the database using the latest data from DPS.",
  )
  @PostMapping("/person/updateAll")
  fun updatePersonCache() {
    personService.updateAllPeople()
  }

  @PutMapping("/person/updatePrisoners")
  @Operation(
    summary = "Update prisoners by their prison numbers",
    tags = ["Admin"],
  )
  fun updateByNumbers(@Parameter(required = true) @Valid @RequestBody prisonNumbers: List<String>) = personService.updatePeople(prisonNumbers)

  @PutMapping("/referrals/updatePom")
  @Operation(
    summary = "Update referrals to update primary and secondary POMs",
    tags = ["Admin"],
  )
  fun updatePoms(): ResponseEntity<String> {
    referralService.getPrisonIdsWithNoPrimaryPom().forEach {
      log.info("START: Updating POMs for prisoner $it")
      try {
        val (primaryPom, secondaryPom) = staffService.getOffenderAllocation(it)
        referralService.updatePoms(it, primaryPom, secondaryPom)
        log.info("FINISH: Updating POMs for prisoner $it")
      } catch (ex: Exception) {
        log.warn("ERROR: Updating POMs for prisoner $it - ${ex.message}", ex)
      }
    }

    return ResponseEntity.status(HttpStatus.OK).body("POMs updated")
  }

  @PutMapping("/referrals/updateLdc")
  @Operation(
    summary = "Update referrals to have hasLdc flag",
    tags = ["Admin"],
  )
  fun updateLdc(): ResponseEntity<String> {
    referralService.getPrisonIdsWithoutLdc().forEach {
      log.info("**** START: Updating LDC for prisoner $it")
      try {
        referralService.updateLdc(it)
        log.info("**** FINISH: Updating LDC for prisoner $it")
      } catch (ex: Exception) {
        log.warn("**** ERROR: Updating LDC for prisoner $it - ${ex.message}", ex)
      }
    }

    return ResponseEntity.status(HttpStatus.OK).body("LDCs updated")
  }

  @DeleteMapping("/clean-up-test-referrals")
  @Operation(
    summary = "Delete referrals and related entries for only ACP_TEST user",
    tags = ["Admin"],
    responses = [
      ApiResponse(responseCode = "204", description = "No Content - The referral was successfully deleted"),
      ApiResponse(
        responseCode = "401",
        description = "The request was unauthorised",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  fun deleteAcpTestReferrals(): ResponseEntity<String> {
    referralService.deleteReferralsForAcpTestUser()
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Referrals deleted")
  }

  @Operation(
    tags = ["Admin"],
    summary = "Update audit and case notes to fix an incorrect status change",
    operationId = "updateAuditAndCaseNotes",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Audit and case notes creation successful"),
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
        responseCode = "500",
        description = "An error occurred when attempting to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/referrals/update-audit-casenotes"],
    consumes = ["application/json"],
  )
  fun updateAuditAndCaseNotes(
    @Parameter(
      description = "Endpoint to write case notes and audit for a referral",
      required = true,
    ) @RequestBody updateAuditCaseNotesRequest: UpdateAuditCaseNotesRequest,
  ): ResponseEntity<String> {
    log.info("********* START: Request received to write case notes and audit log for referralId ${updateAuditCaseNotesRequest.referralId}")

    val referral = referralService.getReferralById(updateAuditCaseNotesRequest.referralId) ?: throw NotFoundException("No Referral found for referralId ${updateAuditCaseNotesRequest.referralId}")
    log.info("********* Fetched referral $referral")
    caseNotesApiService.createCustomCaseNote(referral, updateAuditCaseNotesRequest.referrerUsername)
    log.info("********* Case notes created for referralId ${referral.id}")
    auditService.createInternalAuditRecord(referralId = referral.id, prisonNumber = referral.prisonNumber, auditAction = AuditAction.UPDATE_REFERRAL.name)
    log.info("********* Internal audit record created for referralId ${referral.id}")
    auditService.publishAuditEvent(auditAction = AuditAction.UPDATE_REFERRAL.name, prisonNumber = referral.prisonNumber, referralId = referral.id.toString())
    log.info("********* Audit event published for referralId ${referral.id}")

    return ResponseEntity.status(HttpStatus.OK).body("Case notes and audit updated successful")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
