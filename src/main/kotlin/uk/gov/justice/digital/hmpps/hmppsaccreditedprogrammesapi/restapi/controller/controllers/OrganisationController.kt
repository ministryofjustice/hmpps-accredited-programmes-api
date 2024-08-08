package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.EnabledOrganisation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Organisation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
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
  private val enabledOrganisationService: EnabledOrganisationService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
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
  fun getAllCoursesByOrganisationId(@Parameter(description = "A organisation identifier", required = true) @PathVariable("organisationId") organisationId: kotlin.String): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllOfferingsByOrganisationId(organisationId).filter { !it.withdrawn }
          .map { it.course }
          .map { it.toApi() },
      )

  @Operation(
    tags = ["reference data"],
    summary = "Get list of enabled organisations",
    operationId = "getEnabledOrganisations",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "List of enabled organisations", content = [Content(array = ArraySchema(schema = Schema(implementation = EnabledOrganisation::class)))]),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised."),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access."),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/organisations/enabled"],
    produces = ["application/json"],
  )
  fun getEnabledOrganisations(): ResponseEntity<List<EnabledOrganisation>> =
    ResponseEntity
      .ok(
        enabledOrganisationService.getEnabledOrganisations()
          .map {
            EnabledOrganisation(
              it.code,
              it.description,
            )
          },
      )

  @Operation(
    tags = ["Organisation"],
    summary = "Get all organisations",
    operationId = "getOrganisations",
    description = """Returns a list of organisations with their ID and name""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = [Content(array = ArraySchema(schema = Schema(implementation = Organisation::class)))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
      ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/organisations"],
    produces = ["application/json"],
  )
  fun getOrganisations(): ResponseEntity<List<Organisation>> =
    ResponseEntity
      .ok(
        prisonRegisterApiService.getPrisons().map {
          Organisation(
            code = it.prisonId,
            prisonName = it.prisonName,
          )
        },
      )
}
