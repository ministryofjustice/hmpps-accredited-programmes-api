package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseCreateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisites
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AudienceService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import java.util.UUID
import kotlin.random.Random

@RestController
@Tag(
  name = "Course",
  description = """
    A series of endpoints for maintaining course and offering information.
  """,
)
@Transactional
class CourseController(
  private val courseService: CourseService,
  private val enabledOrganisationService: EnabledOrganisationService,
  private val audienceService: AudienceService,
) {

  @Operation(
    tags = ["Course Offerings"],
    summary = "Add a course offering",
    operationId = "addCourseOffering",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = CourseOffering::class))]),
      ApiResponse(responseCode = "404", description = "No Course found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/courses/{id}/offerings"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun addCourseOffering(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID, @Parameter(description = "", required = true) @RequestBody courseOffering: CourseOffering): ResponseEntity<CourseOffering> {
    val course = courseService.getCourseById(id)
      ?: throw NotFoundException("No Course found at /courses/$id")
    return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createOffering(course, courseOffering))
  }

  @Operation(
    tags = ["Courses"],
    summary = "Create a course",
    operationId = "createCourse",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Return a JSON representation of the created course", content = [Content(schema = Schema(implementation = Course::class))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/courses"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun createCourse(@Parameter(description = "", required = true) @RequestBody courseCreateRequest: CourseCreateRequest): ResponseEntity<Course> {
    // temporary code to generate a unique identifier (look to remove this, it is a hangover from the CSV days):
    val identifier = courseCreateRequest.identifier ?: generateRandom10AlphaString()
    val courseByIdentifier = courseService.getCourseByIdentifier(identifier)

    if (courseByIdentifier != null) {
      throw BusinessException("Course with identifier ${courseCreateRequest.identifier} already exists")
    }

    val audience = audienceService.getAudienceById(courseCreateRequest.audienceId)
      ?: throw BusinessException("Audience with id ${courseCreateRequest.audienceId} does not exist")

    val course = CourseEntity(
      name = courseCreateRequest.name,
      identifier = identifier,
      description = courseCreateRequest.description,
      alternateName = courseCreateRequest.alternateName,
      audience = audience.name,
      audienceColour = audience.colour,
      withdrawn = courseCreateRequest.withdrawn,
    )

    val savedCourse = courseService.save(course)

    return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse.toApi())
  }

  @Operation(
    tags = ["Courses"],
    summary = "delete a course",
    operationId = "deleteCourse",
    description = """Deletes a course from the database. Note you can only delete a course if it's not being used.""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successful delete"),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "The course did not exist", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.DELETE],
    value = ["/courses/{id}"],
  )
  fun deleteCourse(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID): ResponseEntity<Unit> {
    courseService.delete(id)
    return ResponseEntity.ok(null)
  }

  @Operation(
    tags = ["Course Offerings"],
    summary = "Delete a course offering, This can only be done if there are no referrals using this offering.",
    operationId = "deleteCourseOffering",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.DELETE],
    value = ["/courses/{id}/offerings/{offeringId}"],
  )
  fun deleteCourseOffering(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID, @Parameter(description = "An offering identifier", required = true) @PathVariable("offeringId") offeringId: UUID): ResponseEntity<Unit> {
    courseService.deleteCourseOffering(id, offeringId)
    return ResponseEntity.ok(null)
  }

  @Operation(
    tags = ["Courses"],
    summary = "Get all unique course names",
    operationId = "getAllCourseNames",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Return a JSON representation of all unique course names that are not withdrawn.", content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses/course-names"],
    produces = ["application/json"],
  )
  fun getAllCourseNames(@Parameter(description = "flag to include withdrawn") @RequestParam(value = "includeWithdrawn", required = false) includeWithdrawn: Boolean?): ResponseEntity<List<String>> = ResponseEntity
    .ok(
      courseService
        .getCourseNames(includeWithdrawn ?: false),
    )

  @Operation(
    tags = ["Courses"],
    summary = "List all courses",
    operationId = "getAllCourses",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Return a JSON representation of all courses. If withdrawn is set to true, it will all courses (including withdrawn courses). Setting it to false will return only active courses ", content = [Content(array = ArraySchema(schema = Schema(implementation = Course::class)))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses"],
    produces = ["application/json"],
  )
  fun getAllCourses(@Parameter(description = "flag to return withdrawn") @RequestParam(value = "withdrawn", required = false) withdrawn: Boolean?): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllCourses(withdrawn ?: false)
          .map(CourseEntity::toApi),
      )

  @Operation(
    tags = ["Course Offerings"],
    summary = "List all offerings for a course",
    operationId = "getAllOfferingsByCourseId",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = CourseOffering::class)))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses/{id}/offerings"],
    produces = ["application/json"],
  )
  fun getAllOfferingsByCourseId(
    @Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID,
    @Parameter(description = "flag to return withdrawn offerings") @RequestParam(value = "includeWithdrawn", required = false) includeWithdrawn: Boolean?,
    @Parameter(description = "Offerings for a gender") @RequestParam(value = "gender", required = false) gender: Gender?,
  ): ResponseEntity<List<CourseOffering>> {
    val offerings = courseService.getAllOfferings(id, includeWithdrawn ?: false)
    val mappedOfferings = offerings.map { offeringEntity ->
      val enabledOrg = enabledOrganisationService.getEnabledOrganisation(offeringEntity.organisationId) != null
      offeringEntity.toApi(enabledOrg)
    }
    return ResponseEntity.ok(mappedOfferings)
  }

  @Operation(
    tags = ["Courses"],
    summary = "Get all audiences",
    operationId = "getAudiences",
    description = """Returns a list of audiences with their name and colour""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully retrieved list of audience", content = [Content(array = ArraySchema(schema = Schema(implementation = Audience::class)))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses/audiences"],
    produces = ["application/json"],
  )
  fun getAudiences(): ResponseEntity<List<Audience>> = ResponseEntity
    .ok(
      audienceService
        .getAllAudiences().map {
          Audience(
            id = it.id,
            name = it.name,
            colour = it.colour,
          )
        },
    )

  @Operation(
    tags = ["Courses"],
    summary = "Details for a single course",
    operationId = "getCourseById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Course::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses/{id}"],
    produces = ["application/json"],
  )
  fun getCourseById(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID): ResponseEntity<Course> =
    courseService.getNotWithdrawnCourseById(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /courses/$id")

  @Operation(
    tags = ["Courses"],
    summary = "Get a list of prerequisites for a course.",
    operationId = "getCoursePrerequisites",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "", content = [Content(schema = Schema(implementation = CoursePrerequisites::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/courses/{id}/prerequisites"],
    produces = ["application/json"],
  )
  fun getCoursePrerequisites(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID): ResponseEntity<CoursePrerequisites> =
    ResponseEntity.ok(
      CoursePrerequisites(
        courseService
          .getCourseById(id)?.prerequisites?.map { prerequisite ->
            CoursePrerequisite(
              name = prerequisite.name,
              description = prerequisite.description,
            )
          },
      ),
    )

  @Operation(
    tags = ["Courses"],
    summary = "Update a course",
    operationId = "updateCourse",
    description = """Updates the details of a specific course""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successful update", content = [Content(schema = Schema(implementation = Course::class))]),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "You are not authorized to view the resource", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/courses/{id}"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun updateCourse(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID, @Parameter(description = "", required = true) @RequestBody courseUpdateRequest: CourseUpdateRequest): ResponseEntity<Course> {
    val existingCourse = courseService.getCourseById(id)
      ?: throw NotFoundException("No Course found at /courses/$id")

    val updatedCourse = existingCourse.copy(
      name = courseUpdateRequest.name ?: existingCourse.name,
      description = courseUpdateRequest.description ?: existingCourse.description,
      alternateName = courseUpdateRequest.alternateName ?: existingCourse.alternateName,
      listDisplayName = courseUpdateRequest.displayName ?: existingCourse.listDisplayName,
      audience = courseUpdateRequest.audience ?: existingCourse.audience,
      audienceColour = courseUpdateRequest.audienceColour ?: existingCourse.audienceColour,
      withdrawn = courseUpdateRequest.withdrawn ?: existingCourse.withdrawn,
    )

    val savedCourse = courseService.save(updatedCourse)

    return ResponseEntity.ok(savedCourse.toApi())
  }

  @Operation(
    tags = ["Course Offerings"],
    summary = "update a course offering",
    operationId = "updateCourseOffering",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = CourseOffering::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/courses/{id}/offerings"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun updateCourseOffering(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID, @Parameter(description = "", required = true) @RequestBody courseOffering: CourseOffering): ResponseEntity<CourseOffering> {
    val course = courseService.getCourseById(id)
      ?: throw NotFoundException("No Course found at /courses/$id")
    return ResponseEntity.ok(courseService.updateOffering(course, courseOffering))
  }

  @Operation(
    tags = ["Courses"],
    summary = "",
    operationId = "updateCoursePrerequisites",
    description = """Replace all prerequisites for a course""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successful update", content = [Content(schema = Schema(implementation = CoursePrerequisites::class))]),
      ApiResponse(responseCode = "400", description = "Bad input", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
    security = [ SecurityRequirement(name = "bearerAuth") ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/courses/{id}/prerequisites"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun updateCoursePrerequisites(@Parameter(description = "A course identifier", required = true) @PathVariable("id") id: UUID, @Parameter(description = "", required = true) @RequestBody coursePrerequisites: CoursePrerequisites): ResponseEntity<CoursePrerequisites> {
    val course =
      courseService.getNotWithdrawnCourseById(id) ?: throw NotFoundException("No Course found at /courses/$id")
    return ResponseEntity.ok(
      CoursePrerequisites(
        courseService.updateCoursePrerequisites(
          course,
          coursePrerequisites.prerequisites!!.toMutableSet(),
        ),
      ),
    )
  }

  fun generateRandom10AlphaString(): String {
    val chars = ('A'..'Z')
    val charsList = chars.toList()
    return (1..10)
      .map { Random.nextInt(0, charsList.size) }
      .map(charsList::get)
      .joinToString("")
  }
}
