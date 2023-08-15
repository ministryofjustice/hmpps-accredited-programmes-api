package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OfferingsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toApi
import java.util.UUID

@Service
class OfferingsController(private val courseService: CourseService) : OfferingsApiDelegate {

  override fun offeringsIdCourseGet(id: UUID): ResponseEntity<Course> =
    courseService.getCourseForOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")
}
