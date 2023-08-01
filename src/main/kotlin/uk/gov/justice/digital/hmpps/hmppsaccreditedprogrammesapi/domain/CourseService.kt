package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage.Level
import java.util.UUID

@Service
@Transactional
class CourseService(@Autowired val courseRepository: CourseRepository) {
  fun allActiveCourses(): List<CourseEntity> = courseRepository.allActiveCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository.offeringsForCourse(courseId)

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = courseRepository.courseOffering(courseId, offeringId)

  fun updateCourses(courseUpdates: List<CourseUpdate>): List<LineMessage> {
    updateAudiences(courseUpdates)
    val audienceByName: Map<String, Audience> = courseRepository.allAudiences().associateBy(Audience::value)
    val coursesByIdentifier = courseRepository.allCourses().associateBy(CourseEntity::identifier)

    courseUpdates.filter { it.identifier.isNotBlank() }
      .forEach { courseUpdate ->
        when (val persistentCourse = coursesByIdentifier[courseUpdate.identifier]) {
          null -> courseRepository.saveCourse(courseUpdate.toCourseEntity(audienceByName))
          else -> persistentCourse.update(courseUpdate, audienceByName)
        }
      }

    val identifiersInUpdate = courseUpdates.map(CourseUpdate::identifier).toSet()
    val identifiersInRepository = coursesByIdentifier.keys
    val identifiersToWithdraw = identifiersInRepository - identifiersInUpdate
    identifiersToWithdraw.forEach {
      coursesByIdentifier[it]?.withdrawn = true
    }
    return courseUpdates
      .filter { it.identifier.isBlank() }
      .mapIndexed { index, courseUpdate ->
        LineMessage(
          lineNumber = index + 2,
          level = Level.error,
          message = "Missing course identifier",
        )
      }
  }

  private fun CourseUpdate.toCourseEntity(audiences: Map<String, Audience>) =
    CourseEntity(
      name = name,
      identifier = identifier,
      description = description,
      alternateName = alternateName,
      audiences = audienceStrings.mapNotNull(audiences::get).toMutableSet(),
    )

  private fun CourseEntity.update(courseUpdate: CourseUpdate, allAudiences: Map<String, Audience>) {
    withdrawn = false
    name = courseUpdate.name
    alternateName = courseUpdate.alternateName
    description = courseUpdate.description

    val expectedAudiences = allAudiences.filterKeys(courseUpdate.audienceStrings::contains).values.toSet()
    audiences.retainAll(expectedAudiences)
    audiences.addAll(expectedAudiences)
  }

  private fun updateAudiences(courseUpdates: List<CourseUpdate>) {
    val desiredAudienceKeys = courseUpdates.flatMap(CourseUpdate::audienceStrings).toSet()
    val persistentAudienceKeys = courseRepository.allAudiences().map(Audience::value).toSet()
    val newKeys = desiredAudienceKeys - persistentAudienceKeys
    val newAudiences = newKeys.map(::Audience)
    courseRepository.saveAudiences(newAudiences.toSet())
  }

  fun updateAllPrerequisites(updates: List<PrerequisiteUpdate>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    clearPrerequisites(allCourses)
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    updates.forEach { update ->
      update.identifier.split(",").forEach { identifier ->
        coursesByIdentifier[identifier.trim()]?.run {
          prerequisites.add(Prerequisite(name = update.name, description = update.description ?: ""))
        }
      }
    }
    return updates
      .flatMapIndexed { index, update ->
        update.identifier.split(",").map { identifier ->
          when (coursesByIdentifier.containsKey(identifier.trim())) {
            true -> null
            false -> LineMessage(
              lineNumber = indexToCsvRowNumber(index),
              level = Level.error,
              message = "No match for course identifier '$identifier'",
            )
          }
        }
      }.filterNotNull()
  }

  private fun clearPrerequisites(courses: List<CourseEntity>) {
    courses.forEach { it.prerequisites.clear() }
  }

  fun updateAllOfferings(updates: List<OfferingUpdate>): List<LineMessage> {
    val allCourses = courseRepository.allActiveCourses()
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    val updatesByIdentifier = updates.groupBy(OfferingUpdate::identifier)

    coursesByIdentifier.forEach { (identifier, course) ->
      updateOfferingsForCourse(course.offerings, updatesByIdentifier.getOrDefault(identifier, emptyList()))
    }

    return contactEmailWarnings(updates) + unmatchedCourseErrors(updates, coursesByIdentifier)
  }

  private fun updateOfferingsForCourse(offerings: MutableSet<Offering>, offeringUpdates: List<OfferingUpdate>) {
    val newOfferingsByOrganisationId = offeringUpdates.associateBy(OfferingUpdate::prisonId)
    val newOrganisationIds = newOfferingsByOrganisationId.keys

    offerings.retainAll { newOrganisationIds.contains(it.organisationId) }

    val offeringsByOrganisationId = offerings.associateBy(Offering::organisationId)
    newOfferingsByOrganisationId.forEach { (organisationId, offeringUpdate) ->
      when (val offering = offeringsByOrganisationId[organisationId]) {
        null -> offerings.add(
          Offering(
            organisationId = offeringUpdate.prisonId,
            contactEmail = offeringUpdate.contactEmail ?: "",
            secondaryContactEmail = offeringUpdate.secondaryContactEmail,
          ),
        )

        else -> {
          offering.contactEmail = offeringUpdate.contactEmail ?: ""
          offering.secondaryContactEmail = offeringUpdate.secondaryContactEmail
        }
      }
    }
  }

  private fun contactEmailWarnings(newOfferings: List<OfferingUpdate>): List<LineMessage> =
    newOfferings.mapIndexed { index, record ->
      when (record.contactEmail.isNullOrBlank()) {
        true -> LineMessage(
          lineNumber = indexToCsvRowNumber(index),
          level = Level.warning,
          message = "Missing contactEmail for offering with identifier '${record.identifier}' at prisonId '${record.prisonId}'",
        )

        false -> null
      }
    }.filterNotNull()

  private fun unmatchedCourseErrors(replacements: List<OfferingUpdate>, coursesByIdentifier: Map<String, CourseEntity>) =
    replacements
      .mapIndexed { index, record ->
        when (coursesByIdentifier.containsKey(record.identifier)) {
          true -> null
          false -> LineMessage(
            lineNumber = indexToCsvRowNumber(index),
            level = Level.error,
            message = "No course matches offering with identifier '${record.identifier}' and prisonId '${record.prisonId}'",
          )
        }
      }.filterNotNull()

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
