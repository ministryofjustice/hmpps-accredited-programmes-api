package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService

// Admin endpoints not visible via Swagger
@RestController
@RequestMapping("admin")
class AdminController(
  private val referralService: ReferralService,
) {

  // Endpoint to update the cache in the person table
  // this should sparingly as it updates all the people in the database using the latest data from DPS.
  @PostMapping("/person/updateAll")
  fun updatePersonCache() {
    referralService.updateAllPeople()
  }
}
