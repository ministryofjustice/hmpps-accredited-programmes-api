package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OfferingsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer.toOfferingRecord
import java.util.UUID

@Service
class OfferingsController(private val courseService: CourseService) : OfferingsApiDelegate {

  override fun offeringsIdCourseGet(id: UUID): ResponseEntity<Course> =
    courseService.getCourseForOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")

  override fun offeringsOfferingIdGet(offeringId: UUID): ResponseEntity<CourseOffering> =
    courseService.courseOffering(offeringId)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Offering found at /offerings/$offeringId")

  override fun offeringsCsvPut(offeringRecord: List<OfferingRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.updateOfferings(offeringRecord.map(OfferingRecord::toDomain)))

  override fun offeringsCsvGet(): ResponseEntity<List<OfferingRecord>> =
    ResponseEntity.ok(
      courseService
        .allOfferings()
        .map(Offering::toOfferingRecord),
    )
}
