package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
  fun updateByNumbers(@Parameter(required = true) @Valid @RequestBody prisonNumbers: List<String>) =
    personService.updatePeople(prisonNumbers)

  @PutMapping("/referrals/updatePom")
  @Operation(
    summary = "Update referrals to update primary and secondary POMs",
    tags = ["Admin"],
  )
  fun updatePoms() {
    referralService.getPrisonIdsWithNoPrimaryPom().forEach {
      log.info("START: Updating POMs for prisoner $it")
      val (primaryPom, secondaryPom) = staffService.getOffenderAllocation(it)

      referralService.updatePoms(it, primaryPom, secondaryPom)
      log.info("FINISH: Updating POMs for prisoner $it")
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
