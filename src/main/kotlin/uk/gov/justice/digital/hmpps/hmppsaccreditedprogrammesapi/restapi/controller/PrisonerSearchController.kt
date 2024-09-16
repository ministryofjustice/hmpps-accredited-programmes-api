package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService

@RestController
@Tag(
  name = "Prison Search",
)
class PrisonerSearchController(private val peopleSearchApiService: PeopleSearchApiService) {
  @Operation(
    tags = ["Prison"],
    summary = "Details for of multiple prisoners",
    operationId = "getPrisoners",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = PrisonSearchResponse::class))]),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "The client is not authorized to perform this operation.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/prisoner-search"],
    produces = ["application/json"],
  )
  fun getPrisoners(@Parameter(description = "", required = true) @RequestBody prisonIds: List<String>): ResponseEntity<List<Prisoner>> {
    return ResponseEntity.ok(peopleSearchApiService.getPrisoners(prisonIds))
  }
}
