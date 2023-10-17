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
import org.springframework.beans.factory.annotation.Autowired

@Service
class OfferingController
@Autowired
constructor (
  private val courseService: CourseService,
) : OfferingsApiDelegate {
  override fun getCourseByOfferingId(id: UUID): ResponseEntity<Course> =
    courseService.getCourseByOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")

  override fun getOfferingById(id: UUID): ResponseEntity<CourseOffering> =
    courseService.getOfferingById(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Offering found at /offerings/$id")

  override fun uploadOfferingsCsv(offeringRecord: List<OfferingRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.uploadOfferingsCsv(offeringRecord.map(OfferingRecord::toDomain)))

  override fun getOfferingsCsv(): ResponseEntity<List<OfferingRecord>> =
    ResponseEntity.ok(
      courseService
        .getOfferingsCsv()
        .map(Offering::toOfferingRecord),
    )
}
