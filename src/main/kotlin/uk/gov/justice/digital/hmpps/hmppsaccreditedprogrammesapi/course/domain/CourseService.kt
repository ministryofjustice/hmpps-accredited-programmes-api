package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import java.util.UUID

@Service
@Transactional
class CourseService(
  @Autowired
  val courseRepository: CourseRepository,
) {
  fun allCourses(): List<CourseEntity> = courseRepository.allCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun getCourseForOfferingId(offeringId: UUID): CourseEntity? = courseRepository.findCourseByOfferingId(offeringId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository
    .offeringsForCourse(courseId)
    .filterNot(Offering::withdrawn)

  fun courseOffering(offeringId: UUID): Offering? = courseRepository
    .courseOffering(offeringId)
    ?.takeIf { !it.withdrawn }

  fun replaceAllCourses(courseData: List<NewCourse>) {
    courseRepository.clear()
    courseRepository.saveAudiences(courseData.flatMap { audienceStrings(it.audience) }.map(::Audience).toSet())

    val allAudiences: Map<String, Audience> = courseRepository.allAudiences().associateBy { it.value }

    courseData.map {
      CourseEntity(
        name = it.name,
        identifier = it.identifier,
        description = it.description,
        alternateName = it.alternateName,
        audiences = audienceStrings(it.audience).mapNotNull { audienceName -> allAudiences[audienceName] }.toMutableSet(),
        referable = it.referable,
      )
    }.forEach(courseRepository::saveCourse)
  }

  private fun audienceStrings(audience: String): List<String> = audience.split(',').map(String::trim)

  fun replaceAllPrerequisites(replacements: List<NewPrerequisite>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    clearPrerequisites(allCourses)
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    replacements.forEach { record ->
      record.identifier.split(",").forEach { identifier ->
        coursesByIdentifier[identifier.trim()]?.run {
          prerequisites.add(Prerequisite(name = record.name, description = record.description ?: ""))
        }
      }
    }
    return replacements
      .flatMapIndexed { index, record ->
        record.identifier.split(",").map { identifier ->
          when (coursesByIdentifier.containsKey(identifier.trim())) {
            true -> null
            false -> LineMessage(
              lineNumber = indexToCsvRowNumber(index),
              level = LineMessage.Level.error,
              message = "No match for course identifier '$identifier'",
            )
          }
        }
      }.filterNotNull()
  }

  private fun clearPrerequisites(courses: List<CourseEntity>) {
    courses.forEach { it.prerequisites.clear() }
  }

  fun replaceAllOfferings(replacements: List<NewOffering>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    clearOfferings(allCourses)
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    replacements.forEach { record ->
      coursesByIdentifier[record.identifier]?.run {
        addOffering(
          Offering(
            organisationId = record.prisonId,
            contactEmail = record.contactEmail
              ?: "",
            secondaryContactEmail = record.secondaryContactEmail,
          ),
        )
      }
    }

    return contactEmailWarnings(replacements) + unmatchedCourseErrors(replacements, coursesByIdentifier)
  }

  private fun contactEmailWarnings(newOfferings: List<NewOffering>): List<LineMessage> =
    newOfferings.mapIndexed { index, record ->
      when (record.contactEmail.isNullOrBlank()) {
        true -> LineMessage(
          lineNumber = indexToCsvRowNumber(index),
          level = LineMessage.Level.warning,
          message = "Missing contactEmail for offering with identifier '${record.identifier}' at prisonId '${record.prisonId}'",
        )

        false -> null
      }
    }.filterNotNull()

  private fun unmatchedCourseErrors(replacements: List<NewOffering>, coursesByIdentifier: Map<String, CourseEntity>) =
    replacements
      .mapIndexed { index, record ->
        when (coursesByIdentifier.containsKey(record.identifier)) {
          true -> null
          false -> LineMessage(
            lineNumber = indexToCsvRowNumber(index),
            level = LineMessage.Level.error,
            message = "No course matches offering with identifier '${record.identifier}' and prisonId '${record.prisonId}'",
          )
        }
      }.filterNotNull()

  private fun clearOfferings(courses: List<CourseEntity>) {
    courses.forEach { it.clearOfferings() }
  }

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
