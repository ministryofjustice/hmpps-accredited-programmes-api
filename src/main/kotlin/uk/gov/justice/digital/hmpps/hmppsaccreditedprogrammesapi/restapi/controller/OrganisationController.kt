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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Organisation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService

@RestController
@Tag(
  name = "Course",
  description = """
    A series of endpoints for maintaining course and offering information.
  """,
)
class OrganisationController(
  private val courseService: CourseService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val organisationService: OrganisationService,
) {

  @Operation(
    tags = ["Course Offerings"],
    summary = "List all courses for an organisationId",
    operationId = "getAllCoursesByOrganisationId",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = Course::class)))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/organisations/{organisationId}/courses"],
    produces = ["application/json"],
  )
  fun getAllCoursesByOrganisationId(@Parameter(description = "A organisation identifier", required = true) @PathVariable("organisationId") organisationId: String): ResponseEntity<List<Course>> = ResponseEntity
    .ok(
      courseService.getAllOfferingsByOrganisationId(organisationId)
        .map { it.course }
        .map { it.toApi() },
    )

  @Operation(
    tags = ["Organisation"],
    summary = "Get all organisations",
    operationId = "getOrganisations",
    description = """Returns a list of organisations with their ID and name""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = [Content(array = ArraySchema(schema = Schema(implementation = Organisation::class)))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/organisations"],
    produces = ["application/json"],
  )
  fun getOrganisations(): ResponseEntity<List<Organisation>> = ResponseEntity
    .ok(
      prisonRegisterApiService.getPrisons().map {
        Organisation(
          code = it.prisonId,
          prisonName = it.prisonName,
        )
      },
    )

  @Operation(
    tags = ["Organisation"],
    summary = "Retrieve an Organisation by its Organisation code",
    operationId = "getOrganisation",
    description = """Returns an organisation's details""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully retrieved organisation", content = [Content(array = ArraySchema(schema = Schema(implementation = Organisation::class)))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/organisation/{code}"],
    produces = ["application/json"],
  )
  fun getOrganisation(@Parameter(description = "The organisation identifier code", required = true) @PathVariable("code") code: String): ResponseEntity<Organisation> = organisationService.findOrganisationEntityByCode(code)?.let {
    ResponseEntity.ok(
      Organisation(
        code = it.code,
        prisonName = it.name,
        gender = it.gender.name,
      ),
    )
  } ?: throw NotFoundException("No Organisation found at /organisation/$code")
}
