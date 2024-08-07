package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseCreateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisites
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toCourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AudienceService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import java.util.UUID
import kotlin.random.Random

@Service
class CourseController
@Autowired
constructor(
  private val courseService: CourseService,
  private val enabledOrganisationService: EnabledOrganisationService,
  private val audienceService: AudienceService,
) : CoursesApiDelegate {
  override fun getAllCourses(withdrawn: Boolean?): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllCourses(withdrawn ?: false)
          .map(CourseEntity::toApi),
      )

  override fun getCoursesCsv(): ResponseEntity<List<CourseRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllCourses(false)
        .map(CourseEntity::toCourseRecord),
    )

  override fun updateCourses(courseRecord: List<CourseRecord>): ResponseEntity<Unit> {
    courseService.updateCourses(courseRecord.map(CourseRecord::toDomain))
    return ResponseEntity.noContent().build()
  }

  override fun getCoursePrerequisites(id: UUID): ResponseEntity<CoursePrerequisites> =
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

  override fun updateCoursePrerequisites(
    id: UUID,
    coursePrerequisites: CoursePrerequisites,
  ): ResponseEntity<CoursePrerequisites> {
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

  override fun getCourseById(id: UUID): ResponseEntity<Course> =
    courseService.getNotWithdrawnCourseById(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /courses/$id")

  override fun getAllOfferingsByCourseId(id: UUID): ResponseEntity<List<CourseOffering>> {
    val offerings = courseService.getAllOfferingsByCourseId(id)
    val mappedOfferings = offerings.map { offeringEntity ->
      val enabledOrg = enabledOrganisationService.getEnabledOrganisation(offeringEntity.organisationId) != null
      offeringEntity.toApi(enabledOrg)
    }
    return ResponseEntity.ok(mappedOfferings)
  }

  override fun addCourseOffering(
    id: UUID,
    courseOffering: CourseOffering,
  ): ResponseEntity<CourseOffering> {
    val course = courseService.getCourseById(id)
      ?: throw NotFoundException("No Course found at /courses/$id")
    return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createOrUpdateOffering(course, courseOffering))
  }

  override fun updateCourseOffering(
    id: UUID,
    courseOffering: CourseOffering,
  ): ResponseEntity<CourseOffering> {
    val course = courseService.getCourseById(id)
      ?: throw NotFoundException("No Course found at /courses/$id")
    return ResponseEntity.ok(courseService.createOrUpdateOffering(course, courseOffering))
  }

  override fun deleteCourseOffering(id: UUID, offeringId: UUID): ResponseEntity<Unit> {
    courseService.deleteCourseOffering(id, offeringId)
    return ResponseEntity.ok(null)
  }

  override fun getAllCourseNames(includeWithdrawn: Boolean?): ResponseEntity<List<String>> = ResponseEntity
    .ok(
      courseService
        .getCourseNames(includeWithdrawn),
    )

  override fun getAudiences(): ResponseEntity<List<Audience>> = ResponseEntity
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

  override fun updateCourse(id: UUID, courseUpdateRequest: CourseUpdateRequest): ResponseEntity<Course> {
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

  override fun createCourse(courseCreateRequest: CourseCreateRequest): ResponseEntity<Course> {
    // temporary code to generate a unique identifier:
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

  override fun deleteCourse(id: UUID): ResponseEntity<Unit> {
    courseService.delete(id)
    return ResponseEntity.ok(null)
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
