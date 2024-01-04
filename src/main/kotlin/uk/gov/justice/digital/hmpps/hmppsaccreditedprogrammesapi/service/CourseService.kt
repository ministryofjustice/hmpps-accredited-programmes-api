package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AudienceRepository
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
  private val audienceRepository: AudienceRepository,
) {
  fun getAllCourses(): List<CourseEntity> = courseRepository
    .findAll()
    .filterNot { it.withdrawn }

  fun getCourseById(courseId: UUID): CourseEntity? = courseRepository
    .findByIdOrNull(courseId)
    ?.takeIf { !it.withdrawn }
  fun getCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository
    .findByMutableOfferingsId(offeringId)

  fun getAllOfferings(): List<OfferingEntity> = offeringRepository
    .findAll()
    .filterNot { it.withdrawn }

  fun getAllOfferingsByOrganisationId(organisationId: String): List<OfferingEntity> = offeringRepository
    .findAll()
    .filter { it.organisationId == organisationId }

  fun getAllOfferingsByCourseId(courseId: UUID): List<OfferingEntity> = offeringRepository
    .findAllByCourseId(courseId)
    .filterNot { it.withdrawn }

  fun getOfferingById(offeringId: UUID): OfferingEntity? = offeringRepository
    .findByIdOrNull(offeringId)
    ?.takeIf { !it.withdrawn }

  fun updateCourses(courseData: List<CourseUpdate>) {
    updateAudiences(courseData)
    val allAudiences: Map<String, AudienceEntity> = audienceRepository.findAll().associateBy { it.value }
    val coursesByIdentifier = courseRepository.findAll().associateBy(CourseEntity::identifier)
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
    }.forEach(courseRepository::save)

    toUpdate.forEach { courseIdentifier ->
      courseDataByIdentifier[courseIdentifier]?.let { update ->
        val expectedAudienceStrings = audienceStrings(update.audience).toSet()
        coursesByIdentifier[courseIdentifier]?.run {
          withdrawn = false
          name = update.name
          description = update.description
          alternateName = update.alternateName
          referable = update.referable

          val audiencesByValue = audiences.associateBy(AudienceEntity::value)
          val audiencesToAdd = expectedAudienceStrings - audiencesByValue.keys
          val audiencesToRemove = audiencesByValue.keys - expectedAudienceStrings
          audiences.addAll(audiencesToAdd.mapNotNull { allAudiences[it] })
          audiences.removeAll(audiencesToRemove.mapNotNull { allAudiences[it] }.toSet())
        }
      }
    }

    toWithdraw.forEach {
      coursesByIdentifier[it]?.withdrawn = true
    }
  }

  private fun updateAudiences(courseData: List<CourseUpdate>) {
    val desiredAudienceValues = courseData.flatMap { audienceStrings(it.audience) }.toSet()
    val actualAudienceValues = audienceRepository.findAll().map { it.value }.toSet()

    val audienceValuesToSave = desiredAudienceValues - actualAudienceValues
    val audiencesToSave = audienceValuesToSave.map { AudienceEntity(value = it) }.toSet()

    audienceRepository.saveAll(audiencesToSave)
  }

  private fun audienceStrings(audience: String): List<String> = audience.split(',').map(String::trim)

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

  fun updateOfferings(updates: List<OfferingUpdate>): List<LineMessage> {
    val allCourses = courseRepository.findAll()
    val coursesByIdentifier = allCourses.associateBy(CourseEntity::identifier)
    val desiredOfferingsByCourseIdentifier = updates.groupBy(OfferingUpdate::identifier)
    coursesByIdentifier.forEach { (courseIdentifier, course) ->
      updateOfferingsForCourse(course, desiredOfferingsByCourseIdentifier[courseIdentifier] ?: emptyList())
    }

    return contactEmailWarnings(updates) + unmatchedCourseErrors(updates, coursesByIdentifier)
  }

  private fun updateOfferingsForCourse(course: CourseEntity, desiredOfferings: List<OfferingUpdate>) {
    val offeringsByOrganisationId = course.mutableOfferings.associateBy(OfferingEntity::organisationId)
    val updatesByPrisonId = desiredOfferings.associateBy(OfferingUpdate::prisonId)

    val toAdd = updatesByPrisonId.keys - offeringsByOrganisationId.keys
    toAdd.forEach {
      val update = updatesByPrisonId[it]
      val offeringToAdd = OfferingEntity(
        organisationId = it,
        contactEmail = update?.contactEmail ?: "",
        secondaryContactEmail = update?.secondaryContactEmail,
      )

      addOfferingToCourse(course.id!!, offeringToAdd)
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

  fun addOfferingToCourse(courseId: UUID, offering: OfferingEntity) {
    val course = courseRepository.findByIdOrNull(courseId)

    offering.course = course!!
    course.mutableOfferings += offering

    offeringRepository.save(offering)
    courseRepository.save(course)
  }

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }
}
