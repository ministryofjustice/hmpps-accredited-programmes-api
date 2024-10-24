package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
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
  fun getAllCourses(includeWithdrawn: Boolean = false): List<CourseEntity> {
    if (includeWithdrawn) {
      return courseRepository.findAll()
    }
    return courseRepository.findAllByWithdrawnIsFalse()
  }

  fun getCourseNames(includeWithdrawn: Boolean = false) =
    courseRepository.getCourseNames(includeWithdrawn)

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
  fun getAllOfferingsByOrganisationId(organisationId: String): List<OfferingEntity> =
    offeringRepository.findAll().filter { it.organisationId == organisationId }

  fun getAllOfferings(courseId: UUID, includeWithdrawn: Boolean = false): List<OfferingEntity> {
    return if (includeWithdrawn) {
      offeringRepository.findAllByCourseId(courseId)
    } else {
      offeringRepository.findAllByCourseIdAndWithdrawnIsFalse(courseId)
    }
  }

  fun getOfferingById(offeringId: UUID): OfferingEntity? =
    offeringRepository.findByIdOrNull(offeringId)?.takeIf { !it.withdrawn }

  fun updateCoursePrerequisites(
    course: CourseEntity,
    coursePrerequisites: Set<CoursePrerequisite>,
  ): List<CoursePrerequisite>? {
    val courseSaved = courseRepository.save(course.copy(prerequisites = coursePrerequisites.toEntity()))
    return courseSaved.prerequisites.map { it.toApi() }
  }

  fun createOffering(course: CourseEntity, courseOffering: CourseOffering): CourseOffering {
    val validPrisons = prisonRegisterApiService.getPrisons()

    validPrisons.firstOrNull { prison -> prison.prisonId == courseOffering.organisationId }
      ?: throw NotFoundException("No prison found with code ${courseOffering.organisationId}")

    val existingOffering =
      offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(course.id!!, courseOffering.organisationId)

    // validate that there isn't already an offering for this course/organisation

    if (existingOffering != null) {
      throw BusinessException("Offering already exists for course ${course.name} and organisation ${existingOffering.organisationId}")
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

  fun updateOffering(course: CourseEntity, courseOffering: CourseOffering): CourseOffering {
    val validPrisons = prisonRegisterApiService.getPrisons()

    validPrisons.firstOrNull { prison -> prison.prisonId == courseOffering.organisationId }
      ?: throw NotFoundException("No prison found with code ${courseOffering.organisationId}")

    val offeringEntity =
      (
        offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(course.id!!, courseOffering.organisationId)
          ?: throw BusinessException("Offering does not exist for course ${course.name}")
        )

    // validate that there isn't already an offering for this course/organisation

    val updatedOfferingEntity = course.offerings.find { it.id == courseOffering.id }?.copy(
      id = courseOffering.id,
      organisationId = courseOffering.organisationId,
      contactEmail = courseOffering.contactEmail,
      secondaryContactEmail = courseOffering.secondaryContactEmail,
      withdrawn = courseOffering.withdrawn ?: false,
      referable = courseOffering.referable,
    )!!

    updatedOfferingEntity.course = course

    return offeringRepository.save(updatedOfferingEntity).toApi()
  }

  fun deleteCourseOffering(id: UUID, offeringId: UUID) {
    val existingOffering =
      offeringRepository.findByCourseIdAndIdAndWithdrawnIsFalse(id, offeringId)
        ?: throw BusinessException("Offering does not exist")
    // check that the offering isn't being used
    if (referralRepository.countAllByOfferingId(offeringId) > 0) {
      throw BusinessException("Offering is in use and cannot be deleted. This offering should be withdrawn")
    }
    offeringRepository.delete(existingOffering.id!!)
  }
}

fun Set<CoursePrerequisite>.toEntity(): MutableSet<PrerequisiteEntity> {
  return this.map { PrerequisiteEntity(it.name, it.description) }.toMutableSet()
}

fun CoursePrerequisite.toApi(): CoursePrerequisite {
  return CoursePrerequisite(name, description)
}
