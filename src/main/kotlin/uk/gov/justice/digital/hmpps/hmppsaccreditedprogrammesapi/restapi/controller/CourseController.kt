package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.CoursesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toCourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AudienceService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import java.util.UUID

@Service
class CourseController
@Autowired
constructor(
  private val courseService: CourseService,
  private val enabledOrganisationService: EnabledOrganisationService,
  private val audienceService: AudienceService,
) : CoursesApiDelegate {
  override fun getAllCourses(): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllCourses()
          .map(CourseEntity::toApi),
      )

  override fun getCoursesCsv(): ResponseEntity<List<CourseRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllCourses()
        .map(CourseEntity::toCourseRecord),
    )

  override fun updateCourses(courseRecord: List<CourseRecord>): ResponseEntity<Unit> {
    courseService.updateCourses(courseRecord.map(CourseRecord::toDomain))
    return ResponseEntity.noContent().build()
  }

  override fun updatePrerequisites(prerequisiteRecord: List<PrerequisiteRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.updatePrerequisites(prerequisiteRecord.map(PrerequisiteRecord::toDomain)))

  override fun getPrerequisitesCsv(): ResponseEntity<List<PrerequisiteRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllCourses()
        .flatMap { course ->
          course.prerequisites.map { prerequisite ->
            PrerequisiteRecord(
              name = prerequisite.name,
              description = prerequisite.description,
              course = course.name,
              identifier = course.identifier,
            )
          }
        },
    )

  override fun getCoursePrerequisites(id: UUID): ResponseEntity<List<CoursePrerequisite>> =
    ResponseEntity.ok(
      courseService
        .getCourseById(id)?.prerequisites?.map { prerequisite ->
          CoursePrerequisite(
            name = prerequisite.name,
            description = prerequisite.description,
          )
        },
    )

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

  override fun getAllCourseNames(includeWithdrawn: Boolean?): ResponseEntity<List<String>> = ResponseEntity
    .ok(
      courseService
        .getCourseNames(includeWithdrawn),
    )

  override fun getAudiences() = ResponseEntity
    .ok(
      audienceService
        .getAllAudiences().map {
          Audience(
            name = it.name,
            colour = it.colour,
          )
        },
    )

  override fun updateCourse(id: UUID, courseUpdateRequest: CourseUpdateRequest): ResponseEntity<Course> {
    val existingCourse = courseService.getCourseById(id) ?: throw NotFoundException("No Course found at /courses/$id")

    return run {
      courseUpdateRequest.name?.let { existingCourse.name = it }
      courseUpdateRequest.description?.let { existingCourse.description = it }
      courseUpdateRequest.alternateName?.let { existingCourse.alternateName = it }
      courseUpdateRequest.displayName?.let { existingCourse.listDisplayName = it }
      courseUpdateRequest.audience?.let { existingCourse.audience = it }
      courseUpdateRequest.audienceColour?.let { existingCourse.audienceColour = it }
      courseUpdateRequest.withdrawn?.let { existingCourse.withdrawn = it }

      val savedCourse = courseService.save(existingCourse)

      ResponseEntity.ok(
        Course(
          id = savedCourse.id!!,
          name = savedCourse.name,
          description = savedCourse.description ?: "",
          alternateName = savedCourse.alternateName,
          audience = savedCourse.audience,
          audienceColour = savedCourse.audienceColour,
          coursePrerequisites = emptyList(),
          withdrawn = savedCourse.withdrawn,
        ),
      )
    }
  }
}
