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
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonerNumberUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StaffService

@RestController
@RequestMapping("admin")
@Tag(
  name = "Admin",
  description = """
    API endpoints for ad-hoc data refreshing tasks.
  """,
)
class AdminController(
  private val personService: PersonService,
  private val referralService: ReferralService,
  private val staffService: StaffService,
  private val personRepository: PersonRepository,
  private val peopleSearchApiService: PeopleSearchApiService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

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

  @PutMapping("/person/update-prisoner-number")
  @Operation(
    summary = "Update a prisoner's prisoner number following a NOMIS prisoner number merge",
    description = """
      This endpoint updates a prisoner's number in the ACP system after a NOMIS prisoner number merge has occurred.
      It performs the following validations:
      1. Verifies the current prisoner number exists in the ACP database
      2. Verifies the new prisoner number exists in NOMIS
      3. Confirms the prisoner names match between ACP and NOMIS records
      If all validations pass, the prisoner number is updated in the ACP system.
    """,
    tags = ["Admin"],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Prisoner number successfully updated",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Invalid request - prisoner names do not match between systems",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Not Found - either the current prisoner number does not exist in ACP or the new prisoner number does not exist in NOMIS",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  fun updatePrisonerNumber(
    @Parameter(
      required = true,
      description = "Request containing the current and new prisoner numbers for the merge operation",
    )
    @Valid
    @RequestBody prisonerNumberUpdateRequest: PrisonerNumberUpdateRequest,
  ): ResponseEntity<String> {
    // verify that the current prisoner number exists within AcP
    val person = personRepository.findPersonEntityByPrisonNumber(prisonerNumberUpdateRequest.currentPrisonerNumber)
      ?: log.error("Prisoner number: ${prisonerNumberUpdateRequest.currentPrisonerNumber} not found in ACP").also {
        throw NotFoundException("Prisoner number: ${prisonerNumberUpdateRequest.currentPrisonerNumber} not found in ACP")
      }
    // verify that the new prisoner number exists in NOMIS
    val personSearchResponse = peopleSearchApiService.searchPeople(PeopleSearchRequest(prisonerIdentifier = prisonerNumberUpdateRequest.newPrisonerNumber))
//    if (personSearchResponse.isEmpty() || !prisonerNamesMatch(person as PersonEntity, personSearchResponse[0])) {
//      log.error("New Prisoner number: ${prisonerNumberUpdateRequest.newPrisonerNumber} does not exist in NOMIS").also {
//        throw NotFoundException("New Prisoner number: ${prisonerNumberUpdateRequest.newPrisonerNumber} does not exist in NOMIS")
//      }
//    }
    if (personSearchResponse.isEmpty()) {
      log.error("New prisoner number ${prisonerNumberUpdateRequest.newPrisonerNumber} not found in NOMIS")
      throw NotFoundException("New prisoner number: ${prisonerNumberUpdateRequest.newPrisonerNumber} not found in NOMIS")
    }
    if (!prisonerNamesMatch(person as PersonEntity, personSearchResponse[0])) {
      log.error("Prisoner names do not match for new number ${prisonerNumberUpdateRequest.newPrisonerNumber}")
      throw BusinessException("Prisoner names do not match between ACP and NOMIS for prisoner number ${prisonerNumberUpdateRequest.newPrisonerNumber}")
    }
    personService.updatePrisonNumberForPrisoner(prisonerNumberUpdateRequest.newPrisonerNumber, prisonerNumberUpdateRequest.currentPrisonerNumber)
    log.info("SUCCESS - Prisoner number updated from ${prisonerNumberUpdateRequest.currentPrisonerNumber} to ${prisonerNumberUpdateRequest.newPrisonerNumber}")
    return ResponseEntity.status(HttpStatus.OK).body("Prisoner number updated")
  }

  private fun prisonerNamesMatch(person: PersonEntity, response: PeopleSearchResponse) = person.forename.equals(response.firstName, true) &&
    person.surname.equals(response.lastName, true)
}
