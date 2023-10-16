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
import java.util.UUID

@Service
class OfferingsController(private val courseService: CourseService) : OfferingsApiDelegate {
  override fun getCourseByOfferingId(id: UUID): ResponseEntity<Course> =
    courseService.getCourseForOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")

  override fun getOfferingById(id: UUID): ResponseEntity<CourseOffering> =
    courseService.courseOffering(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Offering found at /offerings/$id")

  override fun uploadOfferingsCsv(offeringRecord: List<OfferingRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.updateOfferings(offeringRecord.map(OfferingRecord::toDomain)))

  override fun getOfferingsCsv(): ResponseEntity<List<OfferingRecord>> =
    ResponseEntity.ok(
      courseService
        .allOfferings()
        .map(Offering::toOfferingRecord),
    )
}
