package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OfferingsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toOfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import java.util.UUID

@Service
class OfferingController
@Autowired
constructor(
  private val courseService: CourseService,
  private val enabledOrganisationService: EnabledOrganisationService,
) : OfferingsApiDelegate {
  override fun getCourseByOfferingId(id: UUID): ResponseEntity<Course> =
    courseService.getCourseByOfferingId(id)?.let {
      ResponseEntity.ok(it.toApi())
    } ?: throw NotFoundException("No Course found at /offerings/$id/course")

  override fun getOfferingById(id: UUID): ResponseEntity<CourseOffering> {
    val offeringById = courseService.getOfferingById(id)
    val enabledOrg = enabledOrganisationService.getEnabledOrganisation(offeringById?.organisationId.orEmpty()) != null

    return offeringById?.toApi(enabledOrg)?.let {
      ResponseEntity.ok(it)
    } ?: throw NotFoundException("No Offering found at /offerings/$id")
  }

  override fun updateOfferings(offeringRecord: List<OfferingRecord>): ResponseEntity<List<LineMessage>> =
    ResponseEntity.ok(courseService.updateOfferings(offeringRecord.map(OfferingRecord::toDomain)))

  override fun getOfferingsCsv(): ResponseEntity<List<OfferingRecord>> =
    ResponseEntity.ok(
      courseService
        .getAllOfferings()
        .map(OfferingEntity::toOfferingRecord),
    )
}
