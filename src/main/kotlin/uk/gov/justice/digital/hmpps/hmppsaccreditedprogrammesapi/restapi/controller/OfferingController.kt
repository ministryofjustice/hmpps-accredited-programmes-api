package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import java.util.UUID

@RestController
@Tag(
  name = "Course",
  description = """
    A series of endpoints for maintaining course and offering information.
  """,
)
class OfferingController(
  private val courseService: CourseService,
  private val enabledOrganisationService: EnabledOrganisationService,
) {

  @Operation(
    tags = ["Courses"],
    summary = "Retrieve the course that owns an offering.",
    operationId = "getCourseByOfferingId",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Information about the Course that owns the offering.", content = [Content(schema = Schema(implementation = Course::class))]),
      ApiResponse(responseCode = "401", description = "The request was unauthorised", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access this offering.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "No offering has the supplied id (Not Found).", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/offerings/{id}/course"],
    produces = ["application/json"],
  )
  fun getCourseByOfferingId(@Parameter(description = "The id (UUID) of an offering.", required = true) @PathVariable("id") id: UUID): ResponseEntity<Course> =
    courseService.getCourseByOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")

  @Operation(
    tags = ["Course Offerings"],
    summary = "Details for a single course offering",
    operationId = "getOfferingById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = CourseOffering::class))]),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access this offering.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "Invalid course offering id", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/offerings/{id}"],
    produces = ["application/json"],
  )
  fun getOfferingById(@Parameter(description = "A course offering identifier", required = true) @PathVariable("id") id: UUID): ResponseEntity<CourseOffering> {
    val offeringById = courseService.getOfferingById(id)
    val enabledOrg = enabledOrganisationService.getEnabledOrganisation(offeringById?.organisation?.code.orEmpty()) != null

    return offeringById?.toApi(enabledOrg)?.let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("No Offering found at /offerings/$id")
  }
}
