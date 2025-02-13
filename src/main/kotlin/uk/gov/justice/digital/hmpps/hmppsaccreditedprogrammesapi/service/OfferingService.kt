package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository

@Service
class OfferingService(
  private val courseService: CourseService,
  private val offeringRepository: OfferingRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun findBuildingChoicesOffering(
    courseIntensity: String,
    courseAudience: String,
    organisationId: String,
  ): OfferingEntity {
    val matchingCourses = courseService.getBuildingChoicesCourses()
      .filter { it.intensity == courseIntensity && it.audience == courseAudience }

    if (matchingCourses.isEmpty()) {
      throw IllegalStateException("No courses found for intensity $courseIntensity and audience $courseAudience")
    }
    if (matchingCourses.size > 1) {
      throw IllegalStateException("Multiple courses found for intensity $courseIntensity and audience $courseAudience")
    }

    val courseId = matchingCourses.first().id!!
    return offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId, organisationId)
      ?: throw IllegalStateException("No active offering found for courseId $courseId and organisationId $organisationId")
        .also { log.warn("Unable to determine building choices offering for courseId $courseId and organisationId $organisationId") }
  }
}
