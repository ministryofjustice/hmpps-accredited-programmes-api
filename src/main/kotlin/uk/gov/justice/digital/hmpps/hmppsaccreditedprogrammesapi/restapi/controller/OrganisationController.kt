package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OrganisationsApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService

@Service
class OrganisationController
@Autowired
constructor(
  private val courseService: CourseService,
) : OrganisationsApiDelegate {
  override fun getAllCoursesByOrganisationId(organisationId: String): ResponseEntity<List<Course>> =
    ResponseEntity
      .ok(
        courseService
          .getAllOfferingsByOrganisationId(organisationId)
          .map { it.course }
          .map { it.toApi() },
      )
}
