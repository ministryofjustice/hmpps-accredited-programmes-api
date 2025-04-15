package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonNumberRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PniService
import java.util.UUID

@RestController
@Tag(name = "PNI")
class PNIController(
  private val pniService: PniService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Operation(
    tags = ["PNI"],
    summary = "Get needs and risk data for prisoner",
    operationId = "getPNIByPrisonNumber",
    description = """Get needs (sex, cognitive, relationships & Self Management) and risk data for given prisoner""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = PniScore::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access person.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Invalid prison number",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/PNI/{prisonNumber}"],
    produces = ["application/json"],
  )
  fun getPNIByPrisonNumber(
    @Parameter(
      description = "Prison nomis identifier",
      required = true,
    ) @PathVariable("prisonNumber") prisonNumber: String,
    @Parameter(description = "Gender of the prisoner", required = false) @RequestParam(
      "gender",
      required = false,
    ) gender: String?,
    @Parameter(description = "save pni result to DB", required = false) @RequestParam(
      "savePNI",
      required = false,
    ) savePNI: Boolean = false,
    @Parameter(description = "referral id", required = false) @RequestParam(
      "referralId",
      required = false,
    ) referralId: UUID?,
  ): ResponseEntity<PniScore> = ResponseEntity.ok(
    pniService.getOasysPniScore(
      prisonNumber = prisonNumber,
    ),
  )

  @Operation(
    tags = ["PNI"],
    summary = "Get oays PNI data for prisoner",
    operationId = "getOasysPNIByPrisonNumbers",
    description = """Get comparison of PNI data for given prisoners""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "successful operation",
        content = [Content(schema = Schema(implementation = String::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised. The request was unauthorised.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden.  The client is not authorised to access person.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Invalid prison number",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/comparePniResults"],
    produces = ["application/json"],
  )
  fun getOasysPNIByPrisonNumbers(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody prisonNumberRequest: PrisonNumberRequest,
  ): ResponseEntity<String> {
    log.info("Request received $prisonNumberRequest to compare PNI data for ${prisonNumberRequest.prisonNumbers} prisoners")
    val result: StringBuilder = StringBuilder().append("*********** PNI COMPARISON ***************\n")
    val mismatchedPrisonNumbers = StringBuilder().append("*********** MISMATCHED PRISON NUMBERS ***************\n")
    prisonNumberRequest.prisonNumbers.forEach { prisonId ->
      val pniComparisonMessage = pniService.fetchAndStoreOasysPni(prisonId)
      mismatchedPrisonNumbers.append("$prisonId, ")
      result.append(pniComparisonMessage)
    }
    log.info("Mismatched prison numbers: $mismatchedPrisonNumbers")
    return ResponseEntity.ok(result.toString())
  }
}
