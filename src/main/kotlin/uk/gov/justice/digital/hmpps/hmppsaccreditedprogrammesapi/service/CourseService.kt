package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.ProgrammePathway
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseVariantRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.addAudience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class CourseService
@Autowired
constructor(
  private val courseRepository: CourseRepository,
  private val offeringRepository: OfferingRepository,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val referralRepository: ReferralRepository,
  private val organisationService: OrganisationService,
  private val pniService: PniService,
  private val courseVariantRepository: CourseVariantRepository,
) {
  fun getAllCourses(includeWithdrawn: Boolean = false): List<CourseEntity> {
    if (includeWithdrawn) {
      return courseRepository.findAll()
    }
    return courseRepository.findAllByWithdrawnIsFalse()
  }

  fun getCourseNames(includeWithdrawn: Boolean = false) = courseRepository.getCourseNames(includeWithdrawn)

  fun getNotWithdrawnCourseById(courseId: UUID): CourseEntity? = courseRepository.findByIdOrNull(courseId)?.takeIf { !it.withdrawn }

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

  fun getAllOfferingsByOrganisationId(organisationId: String): List<OfferingEntity> = offeringRepository.findOfferingsByOrganisationIdWithActiveReferrals(organisationId)

  fun getAllOfferings(courseId: UUID, includeWithdrawn: Boolean = false): List<OfferingEntity> = if (includeWithdrawn) {
    offeringRepository.findAllByCourseId(courseId)
  } else {
    offeringRepository.findAllByCourseIdAndWithdrawnIsFalse(courseId)
  }

  fun getOfferingById(offeringId: UUID): OfferingEntity? = offeringRepository.findByIdOrNull(offeringId)

  fun updateCoursePrerequisites(
    course: CourseEntity,
    coursePrerequisites: Set<CoursePrerequisite>,
  ): List<CoursePrerequisite>? {
    course.updatePrerequisites(coursePrerequisites.toEntity())
    val courseSaved = courseRepository.save(course)
    return courseSaved.prerequisites.map { it.toApi() }
  }

  fun createOffering(course: CourseEntity, courseOffering: CourseOffering): CourseOffering {
    val validPrisons = prisonRegisterApiService.getPrisons()

    val prison = validPrisons.firstOrNull { prison -> prison.prisonId == courseOffering.organisationId }
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
      course = course,
    )

    organisationService.createOrganisationIfNotPresent(courseOffering.organisationId, prison)

    val genderForWhichCourseIsOffered = organisationService.findOrganisationEntityByCode(courseOffering.organisationId)?.gender!!
    return offeringRepository.save(offering).toApi(genderForWhichCourseIsOffered)
  }

  fun updateOffering(courseId: UUID, courseOffering: CourseOffering): CourseOffering {
    log.debug(
      "Request to update offering for course with id: {} for prison: {} to referrable: {} and withdrawn: {}",
      courseId,
      courseOffering.organisationId,
      courseOffering.referable,
      courseOffering.withdrawn,
    )
    val course = getCourseById(courseId)
      ?: throw NotFoundException("No Course found with id: $courseId")

    offeringRepository.findByCourseIdAndOrganisationId(course.id!!, courseOffering.organisationId)
      ?: throw BusinessException("Offering does not exist for course ${course.name}")

    val validPrisons = prisonRegisterApiService.getPrisons()

    val prison = validPrisons.firstOrNull { prison -> prison.prisonId == courseOffering.organisationId }
      ?: throw NotFoundException("No prison found with code ${courseOffering.organisationId}")

    val matchedOffering = course.offerings.find { it.id == courseOffering.id }

    matchedOffering?.let {
      it.organisationId = courseOffering.organisationId
      it.contactEmail = courseOffering.contactEmail
      it.secondaryContactEmail = courseOffering.secondaryContactEmail
      it.withdrawn = courseOffering.withdrawn ?: it.withdrawn
      it.referable = courseOffering.referable
    } ?: throw BusinessException("Offering does not exist for course ${course.name}")

    matchedOffering.course = course

    organisationService.createOrganisationIfNotPresent(courseOffering.organisationId, prison)
    val genderForWhichCourseIsOffered = organisationService.findOrganisationEntityByCode(courseOffering.organisationId)?.gender!!
    return offeringRepository.save(matchedOffering).toApi(genderForWhichCourseIsOffered)
  }

  fun deleteCourseOffering(courseId: UUID, offeringId: UUID) {
    val existingOffering =
      offeringRepository.findByCourseIdAndIdAndWithdrawnIsFalse(courseId, offeringId)
        ?: throw BusinessException("Offering $offeringId does not exist")
    // check that the offering isn't being used
    if (referralRepository.countAllByOfferingId(offeringId) > 0) {
      throw BusinessException("Offering is in use and cannot be deleted. This offering should be withdrawn. OfferingId $offeringId CourseId $courseId")
    }
    offeringRepository.delete(existingOffering.id!!)
  }

  fun findBuildingChoicesCourses(courseIds: List<UUID>, audience: String?, gender: Gender) = courseRepository.findBuildingChoicesCourses(courseIds, audience, gender)
  fun mapCourses(findBuildingChoicesCourses: List<CourseEntity>?, gender: Gender): List<Course>? = findBuildingChoicesCourses?.map {
    Course(
      id = it.id!!,
      identifier = it.identifier,
      name = it.name,
      description = it.description,
      alternateName = it.alternateName,
      coursePrerequisites = it.prerequisites.map(PrerequisiteEntity::toApi),
      audience = it.audience,
      audienceColour = it.audienceColour,
      displayName = it.name + addAudience(it.name, it.audience),
      withdrawn = it.withdrawn,
      displayOnProgrammeDirectory = it.displayOnProgrammeDirectory,
      courseOfferings = it.offerings.map { offeringEntity -> offeringEntity.toApi(gender) },
    )
  }

  fun getCourseName(courseId: UUID): String? = courseRepository.findById(courseId).get().name

  fun getCoursesByName(name: String): List<CourseEntity> = courseRepository.findAllByName(name)
  fun getCourses(courseIds: List<UUID>): List<CourseEntity> = courseRepository.findAllById(courseIds)
  fun getAllBuildingChoicesCourses(): List<CourseEntity> {
    val findAllByCourseId = courseVariantRepository.findAll()

    val bcParentCourseIds = findAllByCourseId.map { it.courseId }
    val bcVariantCourseIds = findAllByCourseId.map { it.variantCourseId }

    val buildingChoicesCourseIds = listOf(bcParentCourseIds, bcVariantCourseIds).flatten()

    return getCourses(buildingChoicesCourseIds)
  }

  fun getIntensityOfBuildingChoicesCourse(programmePathway: String): String = when (programmePathway) {
    ProgrammePathway.HIGH_INTENSITY_BC.name -> "high intensity"
    ProgrammePathway.MODERATE_INTENSITY_BC.name -> "moderate intensity"
    else -> throw BusinessException("Building choices course could not be found for programmePathway $programmePathway")
  }

  fun getBuildingChoicesCourseForTransferringReferral(referralId: UUID, programmePathway: String?): Course {
    val referral = referralRepository.findById(referralId).getOrNull() ?: throw NotFoundException("No referral found for id: $referralId")
    val pniResult = programmePathway ?: pniService.getOasysPniScore(prisonNumber = referral.prisonNumber).programmePathway

    val buildingChoicesCourses = getAllBuildingChoicesCourses()
    val audience = referral.offering.course.audience.takeIf { it == "Sexual offence" } ?: "General offence"
    val buildingChoicesIntensity = getIntensityOfBuildingChoicesCourse(pniResult)
    val recommendedBuildingChoicesCourse =
      buildingChoicesCourses.filter { it.audience == audience }
        .firstOrNull { it.name.contains(buildingChoicesIntensity) }
        ?: throw BusinessException("Building choices course could not be found for audience $audience programmePathway $programmePathway buildingChoicesIntensity $buildingChoicesIntensity")

    val organisation: OrganisationEntity =
      organisationService.findOrganisationEntityByCode(referral.offering.organisationId) ?: throw NotFoundException("No organisation found for ${referral.offering.organisationId} referral $referral")

    val bcOfferingMatchingWithReferralOrg: OfferingEntity =
      (
        recommendedBuildingChoicesCourse.offerings.firstOrNull { (it.organisationId == referral.offering.organisationId) && it.referable && !it.withdrawn }
          ?: throw NotFoundException("Building choices course ${recommendedBuildingChoicesCourse.name} not offered at ${organisation.name} for audience $audience")
        )

    val recommendedBuildingChoicesCourseModel = recommendedBuildingChoicesCourse.toApi()
    recommendedBuildingChoicesCourseModel.courseOfferings = listOf(bcOfferingMatchingWithReferralOrg.toApi(organisation.gender))

    return recommendedBuildingChoicesCourseModel
  }

  fun getBuildingChoicesCourseVariants(courseId: UUID, isInAWomensPrison: Boolean, isConvictedOfASexualOffence: Boolean): List<Course>? {
    val findAllByCourseId = courseVariantRepository.findAllByCourseId(courseId)
      ?: throw BusinessException("$courseId is not a Building choices course")

    val listOfBuildingCourseIds: List<UUID> = listOf(findAllByCourseId.variantCourseId, courseId)
    val audience = if (isConvictedOfASexualOffence) "Sexual offence" else "General offence"
    val genderToWhichCourseIsOffered =
      if (isInAWomensPrison) Gender.FEMALE else Gender.MALE

    val audienceBasedOnGender = if (genderToWhichCourseIsOffered == Gender.FEMALE) null else audience

    val buildingChoicesCourses =
      findBuildingChoicesCourses(
        listOfBuildingCourseIds,
        audienceBasedOnGender,
        genderToWhichCourseIsOffered,
      )

    return mapCourses(buildingChoicesCourses, genderToWhichCourseIsOffered)
  }

  fun updateCourse(courseId: UUID, courseUpdateRequest: CourseUpdateRequest): Course = getCourseById(courseId)?.let { existingCourse ->
    existingCourse.name = courseUpdateRequest.name ?: existingCourse.name
    existingCourse.description = courseUpdateRequest.description ?: existingCourse.description
    existingCourse.alternateName = courseUpdateRequest.alternateName ?: existingCourse.alternateName
    existingCourse.listDisplayName = courseUpdateRequest.displayName ?: existingCourse.listDisplayName
    existingCourse.audience = courseUpdateRequest.audience ?: existingCourse.audience
    existingCourse.audienceColour = courseUpdateRequest.audienceColour ?: existingCourse.audienceColour
    existingCourse.withdrawn = courseUpdateRequest.withdrawn ?: existingCourse.withdrawn
    existingCourse.toApi()
  } ?: throw NotFoundException("No Course found with id: $courseId")

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

fun Set<CoursePrerequisite>.toEntity(): MutableSet<PrerequisiteEntity> = this.map { PrerequisiteEntity(it.name, it.description) }.toMutableSet()

fun CoursePrerequisite.toApi(): CoursePrerequisite = CoursePrerequisite(name, description)
