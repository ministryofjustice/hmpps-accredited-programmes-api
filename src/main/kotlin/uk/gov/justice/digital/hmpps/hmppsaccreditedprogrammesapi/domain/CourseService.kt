package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import java.util.UUID

@Service
@Transactional
class CourseService(
  @Autowired
  val courseRepository: MutableCourseRepository,
) {
  fun allCourses(): List<CourseEntity> = courseRepository.allCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository.offeringsForCourse(courseId)

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = courseRepository.courseOffering(courseId, offeringId)

  fun replaceAllCourses(courseData: List<CourseRecord>) {
    courseRepository.clear()
    courseRepository.saveAudiences(courseData.flatMap { audienceStrings(it.audience) }.map(::Audience).toSet())

    val allAudiences: Map<String, Audience> = courseRepository.allAudiences().associateBy { it.value }

    courseData.map {
      CourseEntity(
        name = it.name,
        description = it.description,
        audiences = audienceStrings(it.audience).mapNotNull { audienceName -> allAudiences[audienceName] }.toMutableSet(),
      )
    }.forEach(courseRepository::saveCourse)
  }

  private fun audienceStrings(audience: String): List<String> = audience.split(',').map(String::trim)

  fun replaceAllPrerequisites(replacements: List<PrerequisiteRecord>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    clearPrerequisites(allCourses)
    val coursesByName = allCourses.associateBy(CourseEntity::name)
    replacements.forEach { record ->
      coursesByName[record.course]?.run {
        prerequisites.add(Prerequisite(name = record.name, description = record.description ?: ""))
      }
    }
    return replacements
      .mapIndexed { index, record ->
        when (coursesByName.containsKey(record.course)) {
          true -> null
          false -> LineMessage(
            lineNumber = indexToCsvRowNumber(index),
            level = LineMessage.Level.error,
            message = "No match for course '${record.course}'",
          )
        }
      }.filterNotNull()
  }

  private fun clearPrerequisites(courses: List<CourseEntity>) {
    courses.forEach { it.prerequisites.clear() }
  }

  fun replaceAllOfferings(replacements: List<OfferingRecord>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    clearOfferings(allCourses)
    val coursesByName = allCourses.associateBy(CourseEntity::name)
    replacements.forEach { record ->
      coursesByName[record.course]?.run {
        offerings.add(Offering(organisationId = record.prisonId, contactEmail = record.contactEmail ?: ""))
      }
    }

    return contactEmailWarnings(replacements) + unmatchedCourseErrors(replacements, coursesByName)
  }

  private fun contactEmailWarnings(offeringRecords: List<OfferingRecord>): List<LineMessage> =
    offeringRecords.mapIndexed { index, record ->
      when (record.contactEmail.isNullOrBlank()) {
        true -> LineMessage(
          lineNumber = indexToCsvRowNumber(index),
          level = LineMessage.Level.warning,
          message = "Missing contactEmail for '${record.course}' offering at prisonId '${record.prisonId}'",
        )

        false -> null
      }
    }.filterNotNull()

  private fun unmatchedCourseErrors(replacements: List<OfferingRecord>, coursesByName: Map<String, CourseEntity>) =
    replacements
      .mapIndexed { index, record ->
        when (coursesByName.containsKey(record.course)) {
          true -> null
          false -> LineMessage(
            lineNumber = indexToCsvRowNumber(index),
            level = LineMessage.Level.error,
            message = "No match for course '${record.course}', prisonId '${record.prisonId}'",
          )
        }
      }.filterNotNull()

  private fun clearOfferings(courses: List<CourseEntity>) {
    courses.forEach { it.offerings.clear() }
  }

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
