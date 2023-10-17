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
  fun getAllCourses(): List<CourseEntity> = courseRepository.allCourses().filterNot(CourseEntity::withdrawn)

  fun getCourseById(courseId: UUID): CourseEntity? = courseRepository.course(courseId)?.takeIf { !it.withdrawn }
  fun getCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository.findCourseByOfferingId(offeringId)

  fun getOfferingsCsv(): List<Offering> = courseRepository.allOfferings().filterNot(Offering::withdrawn)

  fun getAllOfferingsByCourseId(courseId: UUID): List<Offering> = courseRepository
    .offeringsForCourse(courseId)
    .filterNot(Offering::withdrawn)

  fun getOfferingById(offeringId: UUID): Offering? = courseRepository
    .courseOffering(offeringId)
    ?.takeIf { !it.withdrawn }

  fun uploadCoursesCsv(courseData: List<CourseUpdate>) {
    updateAudiences(courseData)
    val allAudiences: Map<String, Audience> = courseRepository.allAudiences().associateBy { it.value }
    val coursesByIdentifier = courseRepository.allCourses().associateBy(CourseEntity::identifier)
    val courseDataByIdentifier = courseData.associateBy(CourseUpdate::identifier)

    val toAdd = courseDataByIdentifier.keys - coursesByIdentifier.keys
    val toUpdate = coursesByIdentifier.keys.intersect(courseDataByIdentifier.keys)
    val toWithdraw = coursesByIdentifier.keys - courseDataByIdentifier.keys

    toAdd.mapNotNull {
      courseDataByIdentifier[it]?.let { update ->
        CourseEntity(
          name = update.name,
          identifier = update.identifier,
          description = update.description,
          alternateName = update.alternateName,
          audiences = audienceStrings(update.audience).mapNotNull { audienceName -> allAudiences[audienceName] }.toMutableSet(),
          referable = update.referable,
        )
      }
    }.forEach(courseRepository::saveCourse)

    toUpdate.forEach { courseIdentifier ->
      courseDataByIdentifier[courseIdentifier]?.let { update ->
        val expectedAudienceStrings = audienceStrings(update.audience).toSet()
        coursesByIdentifier[courseIdentifier]?.run {
          withdrawn = false
          name = update.name
          description = update.description
          alternateName = update.alternateName
          referable = update.referable

          val audiencesByValue = audiences.associateBy(Audience::value)
          val audiencesToAdd = expectedAudienceStrings - audiencesByValue.keys
          val audiencesToRemove = audiencesByValue.keys - expectedAudienceStrings
          audiences.addAll(audiencesToAdd.mapNotNull { allAudiences[it] })
          audiences.removeAll(audiencesToRemove.mapNotNull { allAudiences[it] })
        }
      }
    }

    toWithdraw.forEach {
      coursesByIdentifier[it]?.withdrawn = true
    }
  }

  private fun updateAudiences(courseData: List<CourseUpdate>) {
    val desiredAudiences = courseData.flatMap { audienceStrings(it.audience) }.map(::Audience).toSet()
    val actualAudiences = courseRepository.allAudiences()
    courseRepository.saveAudiences(desiredAudiences - actualAudiences)
  }

  private fun audienceStrings(audience: String): List<String> = audience.split(',').map(String::trim)

  fun uploadPrerequisitedCsv(replacements: List<NewPrerequisite>): List<LineMessage> {
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

  fun uploadOfferingsCsv(updates: List<OfferingUpdate>): List<LineMessage> {
    val allCourses = courseRepository.allCourses()
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    val desiredOfferingsByCourseIdentifier = updates.groupBy(OfferingUpdate::identifier)
    coursesByIdentifier.forEach { (courseIdentifier, course) ->
      updateOfferingsForCourse(course, desiredOfferingsByCourseIdentifier[courseIdentifier] ?: emptyList())
    }

    return contactEmailWarnings(updates) + unmatchedCourseErrors(updates, coursesByIdentifier)
  }

  private fun updateOfferingsForCourse(course: CourseEntity, desiredOfferings: List<OfferingUpdate>) {
    val offeringsByOrganisationId = course.offerings.associateBy(Offering::organisationId)
    val updatesByPrisonId = desiredOfferings.associateBy(OfferingUpdate::prisonId)

    val toAdd = updatesByPrisonId.keys - offeringsByOrganisationId.keys
    toAdd.forEach {
      val update = updatesByPrisonId[it]
      course.addOffering(
        Offering(
          organisationId = it,
          contactEmail = update?.contactEmail ?: "",
          secondaryContactEmail = update?.secondaryContactEmail,
        ),
      )
    }

    val toUpdate = updatesByPrisonId.keys.intersect(offeringsByOrganisationId.keys)
    toUpdate.forEach {
      val update = updatesByPrisonId[it]
      offeringsByOrganisationId[it]?.run {
        withdrawn = false
        contactEmail = update?.contactEmail ?: ""
        secondaryContactEmail = update?.secondaryContactEmail
      }
    }

    val toWithdraw = offeringsByOrganisationId.keys - updatesByPrisonId.keys
    toWithdraw.forEach {
      offeringsByOrganisationId[it]?.run { withdrawn = true }
    }
  }

  private fun contactEmailWarnings(offeringUpdates: List<OfferingUpdate>): List<LineMessage> =
    offeringUpdates.mapIndexed { index, record ->
      when (record.contactEmail.isNullOrBlank()) {
        true -> LineMessage(
          lineNumber = indexToCsvRowNumber(index),
          level = LineMessage.Level.warning,
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
            level = LineMessage.Level.error,
            message = "No course matches offering with identifier '${record.identifier}' and prisonId '${record.prisonId}'",
          )
        }
      }.filterNotNull()

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
