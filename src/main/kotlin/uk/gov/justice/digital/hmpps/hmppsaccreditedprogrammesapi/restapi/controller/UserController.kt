package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CaseLoad
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.UserService

@Controller
class UserController(private val userService: UserService) {

  @Operation(
    tags = ["User"],
    summary = "Get list of caseloads for current user",
    operationId = "getCurrentUserCaseloads",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Retrieves the caseloads associated with the authenticated user.", content = [Content(array = ArraySchema(schema = Schema(implementation = CaseLoad::class)))]),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised."),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access."),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/user/me/caseload"],
    produces = ["application/json"],
  )
  fun getCurrentUserCaseloads(@Parameter(description = "Flag to indicate whether to retrieve all caseloads or not. Set to false by default", schema = Schema(defaultValue = "false")) @RequestParam(value = "allCaseloads", required = false, defaultValue = "false") allCaseloads: Boolean): ResponseEntity<List<CaseLoad>> = ResponseEntity.ok(
    userService
      .getCurrentUsersCaseloads(allCaseloads)
      .map {
        CaseLoad(
          caseLoadId = it.caseLoadId,
          description = it.description,
          type = it.type,
          caseloadFunction = it.caseloadFunction,
          currentlyActive = it.currentlyActive,
        )
      },
  )
}
