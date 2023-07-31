package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import java.util.UUID

@Service
@Transactional
class CourseService(@Autowired val courseRepository: CourseRepository) {
  fun allActiveCourses(): List<CourseEntity> = courseRepository.allActiveCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository.offeringsForCourse(courseId)

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = courseRepository.courseOffering(courseId, offeringId)

  fun updateCourses(courseData: List<NewCourse>) {
    updateAudiences(courseData)
    val audienceByName: Map<String, Audience> = courseRepository.allAudiences().associateBy(Audience::value)
    val coursesByIdentifier = courseRepository.allCourses().associateBy(CourseEntity::identifier)

    courseData.forEach { courseRecord ->
      when (val persistentCourse = coursesByIdentifier[courseRecord.identifier]) {
        null -> courseRepository.saveCourse(courseRecord.toCourseEntity(audienceByName))
        else -> persistentCourse.update(courseRecord, audienceByName)
      }
    }

    val identifiersInUpdate = courseData.map(NewCourse::identifier).toSet()
    val identifiersInRepository = coursesByIdentifier.keys
    val identifiersToWithdraw = identifiersInRepository - identifiersInUpdate
    identifiersToWithdraw.forEach {
      coursesByIdentifier[it]?.withdrawn = true
    }
  }

  private fun NewCourse.toCourseEntity(audiences: Map<String, Audience>) =
    CourseEntity(
      name = name,
      identifier = identifier,
      description = description,
      alternateName = alternateName,
      audiences = audienceStrings.mapNotNull(audiences::get).toMutableSet(),
    )

  private fun CourseEntity.update(newCourse: NewCourse, allAudiences: Map<String, Audience>) {
    withdrawn = false
    name = newCourse.name
    alternateName = newCourse.alternateName
    description = newCourse.description

    val expectedAudiences = allAudiences.filterKeys(newCourse.audienceStrings::contains).values.toSet()
    audiences.retainAll(expectedAudiences)
    audiences.addAll(expectedAudiences)
  }

  private fun updateAudiences(courseData: List<NewCourse>) {
    val desiredAudienceKeys = courseData.flatMap(NewCourse::audienceStrings).toSet()
    val persistentAudienceKeys = courseRepository.allAudiences().map(Audience::value).toSet()
    val newKeys = desiredAudienceKeys - persistentAudienceKeys
    val newAudiences = newKeys.map(::Audience)
    courseRepository.saveAudiences(newAudiences.toSet())
  }

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
        offerings.add(
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
    courses.forEach { it.offerings.clear() }
  }

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
