package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseParticipationService
import java.util.UUID

@RestController
@Transactional
class CourseParticipationController(private val courseParticipationService: CourseParticipationService) {
  @Operation(
    tags = ["Course Participations"],
    summary = "Record information about a person's prior participation in a course.",
    operationId = "createCourseParticipation",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "201",
        description = "The course participation information has been added.",
        content = [Content(schema = Schema(implementation = CourseParticipation::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad input",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorized to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/course-participations"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun createCourseParticipation(
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody courseParticipationCreate: CourseParticipationCreate,
  ): ResponseEntity<CourseParticipation> =
    courseParticipationService.createCourseParticipation(courseParticipationCreate.toDomain())?.let {
      ResponseEntity.status(HttpStatus.CREATED).body(it.toApi())
    } ?: throw Exception("Unable to add to course participation")

  @Operation(
    tags = ["Course Participations"],
    summary = "Delete information about a person's participation in a course.",
    operationId = "deleteCourseParticipationById",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "The information about a person's participation in a course has been deleted.",
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorized to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.DELETE],
    value = ["/course-participations/{id}"],
    produces = ["application/json"],
  )
  fun deleteCourseParticipationById(
    @Parameter(
      description = "The unique identifier assigned to this record when it was created.",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<Unit> {
    courseParticipationService.getCourseParticipationById(id)?.let {
      courseParticipationService.deleteCourseParticipationById(id)
      return ResponseEntity.noContent().build()
    } ?: throw NotFoundException("No course participation found for id $id")
  }

  @Operation(
    tags = ["Course Participations"],
    summary = "Return information about a person's participation in a course. Selected by a unique identifier.",
    operationId = "getCourseParticipationById",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The selected course participation record.",
        content = [Content(schema = Schema(implementation = CourseParticipation::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorised to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/course-participations/{id}"],
    produces = ["application/json"],
  )
  fun getCourseParticipationById(
    @Parameter(
      description = "The unique identifier assigned to this record when it was created.",
      required = true,
    ) @PathVariable("id") id: UUID,
  ): ResponseEntity<CourseParticipation> =
    courseParticipationService.getCourseParticipationById(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No course participation found for id $id")

  @Operation(
    tags = ["Course Participations"],
    summary = "Update the information about a person's participation in a course.",
    operationId = "updateCourseParticipationById",
    description = """""",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The information about a person's participation in a course has been updated.",
        content = [Content(schema = Schema(implementation = CourseParticipation::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client is not authorized to perform this operation.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "There is no information for the id, so it cannot be updated.",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
    security = [SecurityRequirement(name = "bearerAuth")],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/course-participations/{id}"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun updateCourseParticipationById(
    @Parameter(
      description = "The unique identifier assigned to this record when it was created.",
      required = true,
    ) @PathVariable("id") id: UUID,
    @Parameter(
      description = "",
      required = true,
    ) @RequestBody courseParticipationUpdate: CourseParticipationUpdate,
  ): ResponseEntity<CourseParticipation> =
    courseParticipationService.updateCourseParticipationById(id, courseParticipationUpdate.toDomain()).let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No course participation found for id $id")
}
