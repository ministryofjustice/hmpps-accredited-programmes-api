package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import java.util.UUID

@Service
@Transactional
class CourseService
@Autowired
constructor(
  private val courseRepository: CourseRepository,
  private val offeringRepository: OfferingRepository,
) {
  fun getAllCourses(): List<CourseEntity> = courseRepository.findAll().filterNot { it.withdrawn }
  fun getCourseNames(includeWithdrawn: Boolean? = true): List<String> {
    return if (includeWithdrawn == null) {
      courseRepository.getCourseNames(true)
    } else {
      courseRepository.getCourseNames(includeWithdrawn)
    }
  }
  fun getCourseById(courseId: UUID): CourseEntity? = courseRepository.findByIdOrNull(courseId)?.takeIf { !it.withdrawn }
  fun getCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository.findByOfferingId(offeringId)
  fun getAllOfferings(): List<OfferingEntity> = offeringRepository.findAll().filterNot { it.withdrawn }
  fun getAllOfferingsByOrganisationId(organisationId: String): List<OfferingEntity> =
    offeringRepository.findAll().filter { it.organisationId == organisationId }

  fun getAllOfferingsByCourseId(courseId: UUID): List<OfferingEntity> =
    offeringRepository.findAllByCourseId(courseId).filterNot { it.withdrawn }

  fun getOfferingById(offeringId: UUID): OfferingEntity? =
    offeringRepository.findByIdOrNull(offeringId)?.takeIf { !it.withdrawn }

  fun updateCourses(courseUpdates: List<CourseUpdate>) {
    val coursesByIdentifier = courseRepository.findAll().associateBy(CourseEntity::identifier)
    val courseDataByIdentifier = courseUpdates.associateBy(CourseUpdate::identifier)

    val toAdd = courseDataByIdentifier.keys - coursesByIdentifier.keys
    val toUpdate = coursesByIdentifier.keys.intersect(courseDataByIdentifier.keys)
    val toWithdraw = coursesByIdentifier.keys - courseDataByIdentifier.keys

    addCourses(toAdd, courseDataByIdentifier)
    updateCourses(toUpdate, courseDataByIdentifier, coursesByIdentifier)
    withdrawCourses(toWithdraw, coursesByIdentifier)
  }

  private fun addCourses(
    toAdd: Set<String>,
    courseDataByIdentifier: Map<String, CourseUpdate>,
  ) {
    val coursesToAdd = toAdd.mapNotNull {
      courseDataByIdentifier[it]?.let { update ->
        CourseEntity(
          name = update.name,
          identifier = update.identifier,
          description = update.description,
          alternateName = update.alternateName,
          audience = update.audience,
          audienceColour = update.audienceColour,
        )
      }
    }

    courseRepository.saveAll(coursesToAdd)
  }

  private fun updateCourses(
    toUpdate: Set<String>,
    courseDataByIdentifier: Map<String, CourseUpdate>,
    coursesByIdentifier: Map<String, CourseEntity>,
  ) {
    toUpdate.forEach { courseIdentifier ->
      courseDataByIdentifier[courseIdentifier]?.let { update ->
        coursesByIdentifier[courseIdentifier]?.run {
          withdrawn = false
          name = update.name
          description = update.description
          alternateName = update.alternateName
          audience = update.audience
        }
      }
    }
  }

  private fun withdrawCourses(
    toWithdraw: Set<String>,
    coursesByIdentifier: Map<String, CourseEntity>,
  ) {
    toWithdraw.forEach {
      coursesByIdentifier[it]?.withdrawn = true
    }
  }

  fun updatePrerequisites(replacements: List<NewPrerequisite>): List<LineMessage> {
    val allCourses = courseRepository.findAll()
    clearPrerequisites(allCourses)
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    replacements.forEach { record ->
      record.identifier.split(",").forEach { identifier ->
        coursesByIdentifier[identifier.trim()]?.run {
          prerequisites.add(PrerequisiteEntity(name = record.name, description = record.description ?: ""))
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

  fun updateOfferings(offeringUpdates: List<OfferingUpdate>): List<LineMessage> {
    val allCourses = courseRepository.findAll()
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    val desiredOfferingsByCourseIdentifier = offeringUpdates.groupBy(OfferingUpdate::identifier)
    coursesByIdentifier.forEach { (courseIdentifier, course) ->
      updateOfferingsForCourse(course, desiredOfferingsByCourseIdentifier[courseIdentifier] ?: emptyList())
    }

    return contactEmailWarnings(offeringUpdates) + unmatchedCourseErrors(offeringUpdates, coursesByIdentifier)
  }

  private fun updateOfferingsForCourse(course: CourseEntity, offeringUpdates: List<OfferingUpdate>) {
    val offeringsByOrganisationId = course.offerings.associateBy(OfferingEntity::organisationId)
    val updatesByPrisonId = offeringUpdates.associateBy(OfferingUpdate::prisonId)

    val toAdd = updatesByPrisonId.keys - offeringsByOrganisationId.keys
    toAdd.forEach {
      val update = updatesByPrisonId[it]
      val offeringToAdd = OfferingEntity(
        organisationId = it,
        contactEmail = update?.contactEmail ?: "",
        secondaryContactEmail = update?.secondaryContactEmail,
        referable = update?.referable ?: true,
      )

      course.addOffering(offeringToAdd)
    }

    val toUpdate = updatesByPrisonId.keys.intersect(offeringsByOrganisationId.keys)
    toUpdate.forEach {
      val update = updatesByPrisonId[it]
      offeringsByOrganisationId[it]?.run {
        withdrawn = false
        contactEmail = update?.contactEmail ?: ""
        secondaryContactEmail = update?.secondaryContactEmail
        referable = update?.referable ?: true
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

  private fun unmatchedCourseErrors(
    offeringUpdates: List<OfferingUpdate>,
    coursesByIdentifier: Map<String, CourseEntity>,
  ) =
    offeringUpdates
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
