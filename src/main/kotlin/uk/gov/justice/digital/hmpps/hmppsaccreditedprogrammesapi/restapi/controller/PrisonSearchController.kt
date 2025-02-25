package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toPrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService

@RestController
@Tag(
  name = "Prison Search",
)
class PrisonSearchController(private val prisonRegisterApiService: PrisonRegisterApiService) {
  @Operation(
    tags = ["Prison"],
    summary = "Details for a single prison",
    operationId = "getPrisonById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = PrisonSearchResponse::class))]),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "The client is not authorized to perform this operation.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/prison-search/{prisonId}"],
    produces = ["application/json"],
  )
  fun getPrisonById(@Parameter(description = "A prison identifier", required = true) @PathVariable("prisonId") prisonId: String): ResponseEntity<PrisonSearchResponse> = prisonRegisterApiService.getPrisonById(prisonId)?.let {
    ResponseEntity.ok(it.toPrisonSearchResponse())
  } ?: throw NotFoundException("No Prison found for $prisonId")

  @Operation(
    tags = ["Prison"],
    summary = "Search for prisons via prison register api by prison id.",
    operationId = "getPrisons",
    description = """""",
    responses = [
      ApiResponse(responseCode = "201", description = "The prisoner search results.", content = [Content(array = ArraySchema(schema = Schema(implementation = PrisonSearchResponse::class)))]),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "The client is not authorized to perform this operation.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/prison-search"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun getPrisons(@Parameter(description = "", required = true) @RequestBody prisonSearchRequest: PrisonSearchRequest): ResponseEntity<List<PrisonSearchResponse>> = ResponseEntity.ok(
    prisonRegisterApiService.getPrisons(prisonSearchRequest.prisonIds)
      .map { it.toPrisonSearchResponse() },
  )
}
