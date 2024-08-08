package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService

@Controller
class SarsDataController(private val subjectAccessRequestService: SubjectAccessRequestService) {

  @Operation(
    tags = ["Subject Access Request"],
    summary = "API call to retrieve SAR information about a person",
    operationId = "subjectAccessRequestGet",
    description = """Either NOMIS Prison Number (PRN) must be provided as part of the request.
* If the product uses the identifier type transmitted in the request, it can respond with its data and HTTP code 200.
* If the product uses the identifier type transmitted in the request but has no data to respond with, it should respond with HTTP code 204
* If the product does not use the identifier type transmitted in the request, it should respond with HTTP code 209.
""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "the sar content",
        content = [Content(schema = Schema(implementation = Any::class))],
      ),
      ApiResponse(responseCode = "204", description = "Request successfully processed - no content found"),
      ApiResponse(responseCode = "209", description = "Subject Identifier is not recognised by this service"),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/subject-access-request"],
    produces = ["application/json"],
  )
  fun subjectAccessRequestGet(
    @Parameter(description = "prison number") @RequestParam(
      value = "prn",
      required = false,
    ) prn: String?,
    @Parameter(description = "from date") @RequestParam(
      value = "fromDate",
      required = false,
    ) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) fromDate: java.time.LocalDate?,
    @Parameter(description = "to date") @RequestParam(
      value = "toDate",
      required = false,
    ) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) toDate: java.time.LocalDate?,
  ): ResponseEntity<Any> {
    if (prn == null) {
      return ResponseEntity(null, null, 209)
    }

    val sarsData = subjectAccessRequestService.getPrisonContentFor(prn, fromDate, toDate)

    return if (sarsData.content.referrals.isEmpty() && sarsData.content.courseParticipation.isEmpty()) {
      ResponseEntity(HttpStatus.NO_CONTENT)
    } else {
      ResponseEntity(sarsData, HttpStatus.OK)
    }
  }
}
