package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import java.util.UUID

@Service
@Transactional
class CourseService
@Autowired
constructor(
  private val courseRepository: CourseRepository,
  private val offeringRepository: OfferingRepository,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val referralRepository: ReferralRepository,
) {
  fun getAllCourses(withdrawn: Boolean): List<CourseEntity> {
    return if (withdrawn) {
      courseRepository.findAll().filter { it.withdrawn }
    } else {
      courseRepository.findAll().filterNot { it.withdrawn }
    }
  }

  fun getCourseNames(includeWithdrawn: Boolean? = true): List<String> {
    return if (includeWithdrawn == null) {
      courseRepository.getCourseNames(true)
    } else {
      courseRepository.getCourseNames(includeWithdrawn)
    }
  }

  fun getNotWithdrawnCourseById(courseId: UUID): CourseEntity? =
    courseRepository.findByIdOrNull(courseId)?.takeIf { !it.withdrawn }

  fun getCourseById(courseId: UUID): CourseEntity? = courseRepository.findByIdOrNull(courseId)
  fun getCourseByIdentifier(identifier: String): CourseEntity? = courseRepository.findByIdentifier(identifier)
  fun save(courseEntity: CourseEntity): CourseEntity = courseRepository.save(courseEntity)

  fun delete(courseId: UUID) {
    val courseEntity = courseRepository.findById(courseId) ?: throw BusinessException("Course does not exist")
    if (offeringRepository.findAllByCourseId(courseId).isNotEmpty()) {
      throw BusinessException("Cannot delete course as offerings exist that use this course.")
    }
    courseRepository.delete(courseEntity.get())
  }

  fun getCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository.findByOfferingId(offeringId)
  fun getAllOfferings(): List<OfferingEntity> = offeringRepository.findAll().filterNot { it.withdrawn }
  fun getAllOfferingsByOrganisationId(organisationId: String): List<OfferingEntity> =
    offeringRepository.findAll().filter { it.organisationId == organisationId }

  fun getAllOfferingsByCourseId(courseId: UUID): List<OfferingEntity> =
    offeringRepository.findAllByCourseId(courseId).filterNot { it.withdrawn }

  fun getOfferingById(offeringId: UUID): OfferingEntity? =
    offeringRepository.findByIdOrNull(offeringId)?.takeIf { !it.withdrawn }

  @Deprecated(
    """Phasing out these CSV methods, leaving them in for now but adding this comment 
    |so we don't get confused with the new endpoints""",
  )
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
              level = LineMessage.Level.Error,
              message = "No match for course identifier '$identifier'",
            )
          }
        }
      }.filterNotNull()
  }

  private fun clearPrerequisites(courses: List<CourseEntity>) {
    courses.forEach { it.prerequisites.clear() }
  }

  @Deprecated(
    """Phasing out these CSV methods, leaving them in for now but adding this comment 
    |so we don't get confused with the new endpoints""",
  )
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
      offeringToAdd.course = course
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
          level = LineMessage.Level.Warning,
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
            level = LineMessage.Level.Error,
            message = "No course matches offering with identifier '${record.identifier}' and prisonId '${record.prisonId}'",
          )
        }
      }.filterNotNull()

  companion object {
    private fun indexToCsvRowNumber(index: Int) = index + 2
  }

  fun updateCoursePrerequisites(
    course: CourseEntity,
    coursePrerequisites: Set<CoursePrerequisite>,
  ): List<CoursePrerequisite>? {
    val courseSaved = courseRepository.save(course.copy(prerequisites = coursePrerequisites.toEntity()))
    return courseSaved.prerequisites.map { it.toApi() }
  }

  fun createOrUpdateOffering(course: CourseEntity, courseOffering: CourseOffering): CourseOffering {
    val validPrisons = prisonRegisterApiService.getPrisons()

    validPrisons.firstOrNull { prison -> prison.prisonId == courseOffering.organisationId }
      ?: throw NotFoundException("No prison found with code ${courseOffering.organisationId}")
    // validate that there isn't already an offering for this course/organisation
    val existingOffering =
      offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(course.id!!, courseOffering.organisationId)
    existingOffering?.let {
      throw BusinessException("Offering already exists for course ${course.name} and organisation ${it.organisationId}")
    }
    val offering = OfferingEntity(
      id = courseOffering.id,
      organisationId = courseOffering.organisationId,
      contactEmail = courseOffering.contactEmail,
      secondaryContactEmail = courseOffering.secondaryContactEmail,
      withdrawn = courseOffering.withdrawn ?: false,
      referable = courseOffering.referable,
    )
    offering.course = course

    return offeringRepository.save(offering).toApi()
  }

  fun deleteCourseOffering(id: UUID, offeringId: UUID) {
    val existingOffering =
      offeringRepository.findByCourseIdAndIdAndWithdrawnIsFalse(id, offeringId)
        ?: throw BusinessException("Offering does not exist")
    // check that the offering isn't being used
    if (referralRepository.countAllByOfferingId(offeringId) > 0) {
      throw BusinessException("Offering is in use and cannot be deleted. This offering should be withdrawn")
    }
    offeringRepository.delete(existingOffering)
  }
}

fun Set<CoursePrerequisite>.toEntity(): MutableSet<PrerequisiteEntity> {
  return this.map { PrerequisiteEntity(it.name, it.description) }.toMutableSet()
}

fun CoursePrerequisite.toApi(): CoursePrerequisite {
  return CoursePrerequisite(name, description)
}
