package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService

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
}
